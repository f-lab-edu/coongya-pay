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
// (워커의 다중 커밋 내구성 검증은 3B에서 별도 정리 전략으로 다룬다)
@SpringBootTest
@Transactional
class ChargeProcessorIntegrationTest {

    @Autowired private ChargeProcessor chargeProcessor;
    @Autowired private WalletRepository walletRepository;
    @Autowired private BankAccountRepository bankAccountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private TransactionEntryRepository transactionEntryRepository;

    @Test
    void 해피패스_처리하면_잔액이_증가하고_COMPLETED된다() {
        // given: 잔액 0 지갑 + 계좌 + CREATED 충전 5만원
        Long userId = uniqueUserId();
        Wallet wallet = walletRepository.save(Wallet.create(userId));
        BankAccount account = bankAccountRepository.save(BankAccount.create(userId, "088", "1112223334445", "김쿵야"));
        BigDecimal amount = BigDecimal.valueOf(50_000);
        Transaction created = transactionRepository.save(
                Transaction.createCharge(wallet.getId(), account.getId(), amount, "쿵야"));

        // when
        chargeProcessor.process(created.getId());

        // then: 거래 COMPLETED
        Transaction processed = transactionRepository.findById(created.getId()).orElseThrow();
        Assertions.assertThat(processed.getStatus()).isSameAs(TransactionStatus.COMPLETED);
        Assertions.assertThat(processed.getCompletedAt()).isNotNull();

        // 잔액 실제 증가 + version = 1
        Wallet after = walletRepository.findByUserId(userId).orElseThrow();
        Assertions.assertThat(after.getBalance()).isEqualByComparingTo(amount);
        Assertions.assertThat(after.getVersion()).isEqualTo(1L);

        // 원장 CREDIT 1행, balance_after/순번 일치
        Optional<TransactionEntry> entry = transactionEntryRepository
                .findByTransactionIdAndEntryType(created.getId(), TransactionEntryType.CREDIT);
        Assertions.assertThat(entry).isPresent();
        Assertions.assertThat(entry.get().getAmount()).isEqualByComparingTo(amount);
        Assertions.assertThat(entry.get().getBalanceAfter()).isEqualByComparingTo(amount);
        Assertions.assertThat(entry.get().getWalletSequence()).isEqualTo(1L);
    }

    @Test
    void 두_번_처리해도_이중_입금되지_않는다() {
        // given
        Long userId = uniqueUserId();
        Wallet wallet = walletRepository.save(Wallet.create(userId));
        BankAccount account = bankAccountRepository.save(BankAccount.create(userId, "088", "1112223334446", "김쿵야"));
        BigDecimal amount = BigDecimal.valueOf(30_000);
        Transaction created = transactionRepository.save(
                Transaction.createCharge(wallet.getId(), account.getId(), amount, "쿵야"));

        // when: 재처리(멱등)
        chargeProcessor.process(created.getId());
        chargeProcessor.process(created.getId());

        // then: 잔액은 한 번만 증가
        Wallet after = walletRepository.findByUserId(userId).orElseThrow();
        Assertions.assertThat(after.getBalance()).isEqualByComparingTo(amount);
        Assertions.assertThat(after.getVersion()).isEqualTo(1L);
    }

    // user_id UNIQUE 충돌을 피하기 위해 나노초 기반 유니크 id (테스트 간 격리)
    private Long uniqueUserId() {
        return System.nanoTime() % 1_000_000_000L;
    }
}
