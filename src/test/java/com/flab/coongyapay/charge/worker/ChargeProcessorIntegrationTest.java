package com.flab.coongyapay.charge.worker;

import com.flab.coongyapay.account.domain.BankAccount;
import com.flab.coongyapay.account.repository.BankAccountRepository;
import com.flab.coongyapay.transaction.domain.Transaction;
import com.flab.coongyapay.transaction.domain.TransactionEntry;
import com.flab.coongyapay.transaction.enums.TransactionEntryType;
import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.repository.TransactionEntryRepository;
import com.flab.coongyapay.transaction.repository.TransactionRepository;
import com.flab.coongyapay.wallet.domain.Wallet;
import com.flab.coongyapay.wallet.repository.WalletRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

// @Transactional: 커밋된 데이터가 공유 Testcontainer를 오염시켜 @MybatisTest와 충돌하는 것을 방지(테스트 후 롤백).
@SpringBootTest
@Transactional
class ChargeProcessorIntegrationTest {

    @Autowired private ChargeProcessor chargeProcessor;
    @Autowired private ChargeClaimService chargeClaimService;
    @Autowired private WalletRepository walletRepository;
    @Autowired private BankAccountRepository bankAccountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private TransactionEntryRepository transactionEntryRepository;

    @Test
    void 선점_후_처리하면_잔액이_증가하고_COMPLETED된다() {
        Long userId = uniqueUserId();
        Wallet wallet = walletRepository.save(Wallet.create(userId));
        BankAccount account = bankAccountRepository.save(BankAccount.create(userId, "088", "1112223334445", "김쿵야"));
        BigDecimal amount = BigDecimal.valueOf(50_000);
        Transaction created = transactionRepository.save(
                Transaction.createCharge(wallet.getId(), account.getId(), amount, "쿵야"));

        // when: 선점(lease) 후 처리
        long leaseToken = claimLeaseToken(created.getId());
        chargeProcessor.process(created.getId(), leaseToken);

        // then
        Transaction processed = transactionRepository.findById(created.getId()).orElseThrow();
        Assertions.assertThat(processed.getStatus()).isSameAs(TransactionStatus.COMPLETED);
        Assertions.assertThat(processed.getCompletedAt()).isNotNull();

        Wallet after = walletRepository.findByUserId(userId).orElseThrow();
        Assertions.assertThat(after.getBalance()).isEqualByComparingTo(amount);
        Assertions.assertThat(after.getVersion()).isEqualTo(1L);

        Optional<TransactionEntry> entry = transactionEntryRepository
                .findByTransactionIdAndEntryType(created.getId(), TransactionEntryType.CREDIT);
        Assertions.assertThat(entry).isPresent();
        Assertions.assertThat(entry.get().getBalanceAfter()).isEqualByComparingTo(amount);
        Assertions.assertThat(entry.get().getWalletSequence()).isEqualTo(1L);
    }

    @Test
    void 두_번_처리해도_이중_입금되지_않는다() {
        Long userId = uniqueUserId();
        Wallet wallet = walletRepository.save(Wallet.create(userId));
        BankAccount account = bankAccountRepository.save(BankAccount.create(userId, "088", "1112223334446", "김쿵야"));
        BigDecimal amount = BigDecimal.valueOf(30_000);
        Transaction created = transactionRepository.save(
                Transaction.createCharge(wallet.getId(), account.getId(), amount, "쿵야"));

        long leaseToken = claimLeaseToken(created.getId());
        chargeProcessor.process(created.getId(), leaseToken);
        chargeProcessor.process(created.getId(), leaseToken);

        Wallet after = walletRepository.findByUserId(userId).orElseThrow();
        Assertions.assertThat(after.getBalance()).isEqualByComparingTo(amount);
        Assertions.assertThat(after.getVersion()).isEqualTo(1L);
    }

    @Test
    void 리스가_재선점되면_스테일_leaseToken_처리는_펜싱된다() {
        Long userId = uniqueUserId();
        Wallet wallet = walletRepository.save(Wallet.create(userId));
        BankAccount account = bankAccountRepository.save(BankAccount.create(userId, "088", "1112223334447", "김쿵야"));
        BigDecimal amount = BigDecimal.valueOf(20_000);
        Transaction created = transactionRepository.save(
                Transaction.createCharge(wallet.getId(), account.getId(), amount, "쿵야"));

        // 워커 A가 선점 → staleToken
        long staleToken = claimLeaseToken(created.getId());
        // 리스 만료 후 워커 B가 재선점 → lease_token 증가
        transactionRepository.acquireLease(created.getId(), 30);
        long freshToken = transactionRepository.findLeaseToken(created.getId());
        Assertions.assertThat(freshToken).isGreaterThan(staleToken);

        // 스테일 토큰으로 처리 시도 → 펜싱되어 아무 것도 전이/입금되지 않음
        chargeProcessor.process(created.getId(), staleToken);

        Transaction stillCreated = transactionRepository.findById(created.getId()).orElseThrow();
        Assertions.assertThat(stillCreated.getStatus()).isSameAs(TransactionStatus.CREATED);
        Assertions.assertThat(walletRepository.findByUserId(userId).orElseThrow().getBalance())
                .isEqualByComparingTo(BigDecimal.ZERO);

        // 최신 토큰으로 처리하면 정상 완료
        chargeProcessor.process(created.getId(), freshToken);
        Assertions.assertThat(transactionRepository.findById(created.getId()).orElseThrow().getStatus())
                .isSameAs(TransactionStatus.COMPLETED);
        Assertions.assertThat(walletRepository.findByUserId(userId).orElseThrow().getBalance())
                .isEqualByComparingTo(amount);
    }

    private long claimLeaseToken(Long transactionId) {
        return chargeClaimService.claimBatch(50, 30).stream()
                .filter(c -> c.id().equals(transactionId))
                .findFirst()
                .orElseThrow()
                .leaseToken();
    }

    private Long uniqueUserId() {
        return System.nanoTime() % 1_000_000_000L;
    }
}
