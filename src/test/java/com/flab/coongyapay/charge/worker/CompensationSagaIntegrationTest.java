package com.flab.coongyapay.charge.worker;

import com.flab.coongyapay.account.domain.BankAccount;
import com.flab.coongyapay.account.repository.BankAccountRepository;
import com.flab.coongyapay.bank.BankMaintenancePolicy;
import com.flab.coongyapay.transaction.domain.Transaction;
import com.flab.coongyapay.transaction.enums.TransactionFailureReason;
import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.enums.TransactionType;
import com.flab.coongyapay.transaction.repository.TransactionEntryRepository;
import com.flab.coongyapay.transaction.repository.TransactionRepository;
import com.flab.coongyapay.wallet.domain.Wallet;
import com.flab.coongyapay.wallet.repository.WalletRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

/**
 * 보상 사가는 다중 물리 트랜잭션에 걸쳐 있어 @Transactional(롤백) 방식이 부적합하다.
 * (내부 credit의 @Transactional 예외가 공유 트랜잭션을 rollback-only로 오염) → 비트랜잭션 + 수동 정리.
 */
@SpringBootTest
class CompensationSagaIntegrationTest {

    private static final BigDecimal MAX_BALANCE = BigDecimal.valueOf(2_000_000);

    @Autowired private ChargeProcessor chargeProcessor;
    @Autowired private ChargeClaimService chargeClaimService;
    @Autowired private CompensationProcessor compensationProcessor;
    @Autowired private CompensationClaimService compensationClaimService;
    @Autowired private WalletRepository walletRepository;
    @Autowired private BankAccountRepository bankAccountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private TransactionEntryRepository transactionEntryRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @MockitoBean private BankMaintenancePolicy bankMaintenancePolicy;

    @BeforeEach
    void setUp() {
        when(bankMaintenancePolicy.isMaintenanceTime()).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        // 비트랜잭션 커밋 데이터 정리(공유 Testcontainer 오염 방지). FK 제약 없음 → 순서 무관.
        jdbcTemplate.execute("DELETE FROM transaction_entry");
        jdbcTemplate.execute("DELETE FROM transaction");
        jdbcTemplate.execute("DELETE FROM bank_account");
        jdbcTemplate.execute("DELETE FROM wallet");
    }

    @Test
    void 크레딧_한도초과_영구실패면_보상거래로_환불되고_부모는_FAILED_REFUNDED() {
        // given: 잔액이 이미 한도(2,000,000)인 지갑 + CREATED 충전 10,000 (크레딧 시점 한도 초과 유발)
        Long userId = uniqueUserId();
        Wallet wallet = walletRepository.save(Wallet.create(userId));
        walletRepository.updateBalanceAndVersion(wallet.getId(), MAX_BALANCE, 0);
        BankAccount account = bankAccountRepository.save(BankAccount.create(userId, "088", "2223334440001", "김쿵야"));
        BigDecimal amount = BigDecimal.valueOf(10_000);
        Long chargeId = transactionRepository.save(
                Transaction.createCharge(wallet.getId(), account.getId(), amount, "쿵야")).getId();

        // when 1: 충전 처리 → 출금 성공 → 크레딧 한도초과 → 보상 개시
        chargeProcessor.process(chargeId, claimCharge(chargeId));

        // then 1: 부모 COMPENSATING, 자식 보상거래 생성, 잔액 불변, CREDIT 없음
        Assertions.assertThat(transactionRepository.findById(chargeId).orElseThrow().getStatus())
                .isSameAs(TransactionStatus.COMPENSATING);
        Transaction compensation = transactionRepository.findByParentTransactionId(chargeId).orElseThrow();
        Assertions.assertThat(compensation.getTransactionType()).isSameAs(TransactionType.COMPENSATION);
        Assertions.assertThat(compensation.getStatus()).isSameAs(TransactionStatus.CREATED);
        Assertions.assertThat(compensation.getAmount()).isEqualByComparingTo(amount);
        Assertions.assertThat(transactionEntryRepository.existsCredit(chargeId)).isFalse();
        Assertions.assertThat(walletRepository.findByUserId(userId).orElseThrow().getBalance())
                .isEqualByComparingTo(MAX_BALANCE);

        // when 2: 보상 처리 → 환불 성공 → COMPLETED → 부모 FAILED(REFUNDED)
        compensationProcessor.process(compensation.getId(), claimCompensation(compensation.getId()));

        // then 2
        Assertions.assertThat(transactionRepository.findById(compensation.getId()).orElseThrow().getStatus())
                .isSameAs(TransactionStatus.COMPLETED);
        Transaction parent = transactionRepository.findById(chargeId).orElseThrow();
        Assertions.assertThat(parent.getStatus()).isSameAs(TransactionStatus.FAILED);
        Assertions.assertThat(parent.getFailureReason()).isSameAs(TransactionFailureReason.REFUNDED);
        // 환불이므로 지갑 잔액은 여전히 불변
        Assertions.assertThat(walletRepository.findByUserId(userId).orElseThrow().getBalance())
                .isEqualByComparingTo(MAX_BALANCE);
    }

    private long claimCharge(Long id) {
        return chargeClaimService.claimBatch(50, 30).stream()
                .filter(c -> c.id().equals(id)).findFirst().orElseThrow().leaseToken();
    }

    private long claimCompensation(Long id) {
        return compensationClaimService.claimBatch(50, 30).stream()
                .filter(c -> c.id().equals(id)).findFirst().orElseThrow().leaseToken();
    }

    private Long uniqueUserId() {
        return System.nanoTime() % 1_000_000_000L;
    }
}
