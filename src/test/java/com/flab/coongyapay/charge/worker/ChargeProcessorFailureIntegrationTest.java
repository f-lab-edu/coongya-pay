package com.flab.coongyapay.charge.worker;

import com.flab.coongyapay.account.domain.BankAccount;
import com.flab.coongyapay.account.repository.BankAccountRepository;
import com.flab.coongyapay.bank.*;
import com.flab.coongyapay.transaction.domain.Transaction;
import com.flab.coongyapay.transaction.enums.TransactionFailureReason;
import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.repository.TransactionRepository;
import com.flab.coongyapay.wallet.domain.Wallet;
import com.flab.coongyapay.wallet.repository.WalletRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class ChargeProcessorFailureIntegrationTest {

    @Autowired private ChargeProcessor chargeProcessor;
    @Autowired private ChargeClaimService chargeClaimService;
    @Autowired private WalletRepository walletRepository;
    @Autowired private BankAccountRepository bankAccountRepository;
    @Autowired private TransactionRepository transactionRepository;

    @MockitoBean private BankClient bankClient;

    // 자정(점검시간) 실행 시 flaky 방지: 기본 false, 점검 테스트만 true로 스텁
    @MockitoBean private BankMaintenancePolicy bankMaintenancePolicy;

    @BeforeEach
    void setUp() {
        when(bankMaintenancePolicy.isMaintenanceTime()).thenReturn(false);
    }

    @Test
    void 은행이_출금_거절하면_FAILED_WITHDRAWAL_REJECTED() {
        doThrow(BankWithdrawalRejectedException.class).when(bankClient).withdraw(any(), any(), any(), any());
        Long id = createdChargeId(BigDecimal.valueOf(10_000), "1112223330001");

        long token = claimLeaseToken(id);
        chargeProcessor.process(id, token);

        Transaction tx = transactionRepository.findById(id).orElseThrow();
        Assertions.assertThat(tx.getStatus()).isSameAs(TransactionStatus.FAILED);
        Assertions.assertThat(tx.getFailureReason()).isSameAs(TransactionFailureReason.WITHDRAWAL_REJECTED);
    }

    @Test
    void 출금_결과_불명이면_UNKNOWN() {
        doThrow(BankSystemException.class).when(bankClient).withdraw(any(), any(), any(), any());
        Long id = createdChargeId(BigDecimal.valueOf(10_000), "1112223330002");

        long token = claimLeaseToken(id);
        chargeProcessor.process(id, token);

        Assertions.assertThat(transactionRepository.findById(id).orElseThrow().getStatus())
                .isSameAs(TransactionStatus.UNKNOWN);
    }

    @Test
    void UNKNOWN_대사에서_출금확인되면_크레딧까지_완료() {
        Long userId = uniqueUserId();
        Wallet wallet = walletRepository.save(Wallet.create(userId));
        BankAccount account = bankAccountRepository.save(BankAccount.create(userId, "088", "1112223330003", "김쿵야"));
        BigDecimal amount = BigDecimal.valueOf(40_000);
        Long id = transactionRepository.save(Transaction.createCharge(wallet.getId(), account.getId(), amount, "쿵야")).getId();

        doThrow(BankSystemException.class).when(bankClient).withdraw(any(), any(), any(), any());

        long token = claimLeaseToken(id);
        chargeProcessor.process(id, token); // → UNKNOWN

        when(bankClient.getWithdrawalStatus(any())).thenReturn(BankWithdrawalStatus.WITHDRAWN);

        // 대사: 은행이 "출금됨" 응답 → 재개
        chargeProcessor.process(id, token);

        Assertions.assertThat(transactionRepository.findById(id).orElseThrow().getStatus())
                .isSameAs(TransactionStatus.COMPLETED);
        Assertions.assertThat(walletRepository.findByUserId(userId).orElseThrow().getBalance())
                .isEqualByComparingTo(amount);
    }

    @Test
    void UNKNOWN_대사에서_미출금_freshness이내면_재출금_의도로_전이() {
        doThrow(BankSystemException.class).when(bankClient).withdraw(any(), any(), any(), any());
        Long id = createdChargeId(BigDecimal.valueOf(10_000), "1112223330004");

        long token = claimLeaseToken(id);
        chargeProcessor.process(id, token); // → UNKNOWN

        when(bankClient.getWithdrawalStatus(any())).thenReturn(BankWithdrawalStatus.NOT_WITHDRAWN);

        chargeProcessor.process(id, token);

        Assertions.assertThat(transactionRepository.findById(id).orElseThrow().getStatus())
                .isSameAs(TransactionStatus.WITHDRAWING);
    }

    @Test
    void UNKNOWN_대사가_계속_불명이면_재조회_상한_초과시_NEEDS_REVIEW() {
        doThrow(BankSystemException.class).when(bankClient).withdraw(any(), any(), any(), any());
        Long id = createdChargeId(BigDecimal.valueOf(10_000), "1112223330005");

        long token = claimLeaseToken(id);
        chargeProcessor.process(id, token); // → UNKNOWN

        when(bankClient.getWithdrawalStatus(any())).thenReturn(BankWithdrawalStatus.UNKNOWN);
        for (int i = 0; i < 5; i++) {
            chargeProcessor.process(id, token); // 재조회 5회 → 상한 초과
        }

        Assertions.assertThat(transactionRepository.findById(id).orElseThrow().getStatus())
                .isSameAs(TransactionStatus.NEEDS_REVIEW);
    }

    @Test
    void 실행시점_점검시간이면_출금없이_FAILED_BANK_MAINTENANCE() {
        when(bankMaintenancePolicy.isMaintenanceTime()).thenReturn(true);
        Long id = createdChargeId(BigDecimal.valueOf(10_000), "1112223330006");

        long token = claimLeaseToken(id);
        chargeProcessor.process(id, token);

        Transaction tx = transactionRepository.findById(id).orElseThrow();
        Assertions.assertThat(tx.getStatus()).isSameAs(TransactionStatus.FAILED);
        Assertions.assertThat(tx.getFailureReason()).isSameAs(TransactionFailureReason.BANK_MAINTENANCE);
        verify(bankClient, never()).withdraw(any(), any(), any(), any());
    }

    private Long createdChargeId(BigDecimal amount, String accountNumber) {
        Long userId = uniqueUserId();
        Wallet wallet = walletRepository.save(Wallet.create(userId));
        BankAccount account = bankAccountRepository.save(BankAccount.create(userId, "088", accountNumber, "김쿵야"));
        return transactionRepository.save(Transaction.createCharge(wallet.getId(), account.getId(), amount, "쿵야")).getId();
    }

    private long claimLeaseToken(Long transactionId) {
        return chargeClaimService.claimBatch(50, 30).stream()
                .filter(c -> c.id().equals(transactionId))
                .findFirst().orElseThrow().leaseToken();
    }

    private Long uniqueUserId() {
        return System.nanoTime() % 1_000_000_000L;
    }
}
