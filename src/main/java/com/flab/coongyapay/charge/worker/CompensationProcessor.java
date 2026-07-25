package com.flab.coongyapay.charge.worker;

import com.flab.coongyapay.account.domain.BankAccount;
import com.flab.coongyapay.account.repository.BankAccountRepository;
import com.flab.coongyapay.bank.BankClient;
import com.flab.coongyapay.bank.BankRefundStatus;
import com.flab.coongyapay.bank.BankSystemException;
import com.flab.coongyapay.bank.BankWithdrawalRejectedException;
import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.transaction.domain.Transaction;
import com.flab.coongyapay.transaction.enums.TransactionFailureReason;
import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.enums.TransactionType;
import com.flab.coongyapay.transaction.mapper.dto.RetryStateDto;
import com.flab.coongyapay.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 보상(환불) 거래 상태머신 B.
 * CREATED → REFUNDING → (은행 환불) → COMPLETED → 부모 CHARGE COMPENSATING→FAILED(REFUNDED).
 * 불명(UNKNOWN)은 대사로 확정, 재조회 상한 초과 시 NEEDS_REVIEW(수동 개입, 부모는 COMPENSATING 유지).
 */
@Component
@RequiredArgsConstructor
public class CompensationProcessor {

    private static final int REQUERY_CEILING = 5;
    private static final int BASE_BACKOFF_SECONDS = 30;

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankClient bankClient;
    private final Clock clock;

    public void process(Long transactionId, long leaseToken) {
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction == null || transaction.getTransactionType() != TransactionType.COMPENSATION) {
            return;
        }

        switch (transaction.getStatus()) {
            case CREATED -> startRefund(transaction, leaseToken);
            case REFUNDING -> executeRefund(transaction, leaseToken);
            case UNKNOWN -> reconcileRefund(transaction, leaseToken);
            default -> { /* terminal 등: no-op */ }
        }
    }

    private void startRefund(Transaction transaction, long leaseToken) {
        Long id = transaction.getId();
        boolean moved = transactionRepository.updateStatusFenced(id, TransactionStatus.CREATED,
                TransactionStatus.REFUNDING, null, null, leaseToken);
        if (!moved) {
            return;
        }
        transactionRepository.assignExternalIdempotencyKey(id, externalIdempotencyKey(id));
        executeRefund(transaction, leaseToken);
    }

    private void executeRefund(Transaction transaction, long leaseToken) {
        Long id = transaction.getId();
        try {
            BankAccount account = bankAccountRepository.findById(transaction.getAccountId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
            bankClient.refund(account.getBankCode(), account.getAccountNumber(),
                    transaction.getAmount(), externalIdempotencyKey(id));
        } catch (BankWithdrawalRejectedException | BankSystemException e) {
            // 환불 실패/불명 → UNKNOWN(대사로 확정). 돈을 돌려줘야 하므로 실패로 종결하지 않음.
            transactionRepository.updateStatusFenced(id, TransactionStatus.REFUNDING,
                    TransactionStatus.UNKNOWN, null, null, leaseToken);
            return;
        }
        boolean done = transactionRepository.updateStatusFenced(id, TransactionStatus.REFUNDING,
                TransactionStatus.COMPLETED, null, now(), leaseToken);
        if (done) {
            completeParent(transaction.getParentTransactionId());
        }
    }

    private void reconcileRefund(Transaction transaction, long leaseToken) {
        Long id = transaction.getId();
        BankRefundStatus status = bankClient.getRefundStatus(externalIdempotencyKey(id));
        switch (status) {
            case REFUNDED -> {
                boolean done = transactionRepository.updateStatusFenced(id, TransactionStatus.UNKNOWN,
                        TransactionStatus.COMPLETED, null, now(), leaseToken);
                if (done) {
                    completeParent(transaction.getParentTransactionId());
                }
            }
            case NOT_REFUNDED ->
                // 미환불 확정 → 재환불 의도(UNKNOWN → REFUNDING). 돈은 반드시 돌려줘야 함.
                    transactionRepository.updateStatusFenced(id, TransactionStatus.UNKNOWN,
                            TransactionStatus.REFUNDING, null, null, leaseToken);
            case UNKNOWN -> {
                transactionRepository.incrementRequeryCount(id, leaseToken);
                RetryStateDto retry = transactionRepository.findRetryState(id);
                if (retry.getRequeryCount() >= REQUERY_CEILING) {
                    // 상한 초과 → 수동 개입. 부모는 COMPENSATING 유지(이중 환불 방지).
                    transactionRepository.updateStatusFenced(id, TransactionStatus.UNKNOWN,
                            TransactionStatus.NEEDS_REVIEW, null, null, leaseToken);
                } else {
                    transactionRepository.scheduleRetry(id, backoffSeconds(retry.getRetryCount()), leaseToken);
                }
            }
        }
    }

    // 자식 보상 완료 → 부모 CHARGE COMPENSATING → FAILED(REFUNDED)
    private void completeParent(Long parentTransactionId) {
        if (parentTransactionId == null) {
            return;
        }
        transactionRepository.updateStatus(parentTransactionId, TransactionStatus.COMPENSATING,
                TransactionStatus.FAILED, TransactionFailureReason.REFUNDED, now());
    }

    private int backoffSeconds(int retryCount) {
        return BASE_BACKOFF_SECONDS * (1 << Math.min(retryCount, 3));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private String externalIdempotencyKey(Long transactionId) {
        return "refund-" + transactionId;
    }
}
