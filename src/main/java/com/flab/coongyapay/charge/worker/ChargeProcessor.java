package com.flab.coongyapay.charge.worker;

import com.flab.coongyapay.account.domain.BankAccount;
import com.flab.coongyapay.account.repository.BankAccountRepository;
import com.flab.coongyapay.bank.BankClient;
import com.flab.coongyapay.bank.BankSystemException;
import com.flab.coongyapay.bank.BankWithdrawalRejectedException;
import com.flab.coongyapay.bank.BankWithdrawalStatus;
import com.flab.coongyapay.bank.BankMaintenancePolicy;
import com.flab.coongyapay.charge.service.ChargeCreditService;
import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.transaction.domain.Transaction;
import com.flab.coongyapay.transaction.enums.TransactionFailureReason;
import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.mapper.dto.RetryStateDto;
import com.flab.coongyapay.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * CHARGE 거래 상태머신을 전진시키는 워커.
 * 해피: CREATED → WITHDRAWING → WITHDRAWN → DEPOSITING → COMPLETED.
 * 실패/불명: 4xx→FAILED(WITHDRAWAL_REJECTED), timeout/5xx→UNKNOWN(대사로 확정),
 *           크레딧 transient→백오프 재시도, 영구→COMPENSATING(보상은 3D).
 *
 * 원칙: 은행 호출은 트랜잭션/락 밖, 크레딧은 wallet FOR UPDATE 안. 모든 기록은 leaseToken 펜싱.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChargeProcessor {

    private static final int FRESHNESS_MINUTES = 30;   // 미출금 확정 시 재시도 허용 window
    private static final int REQUERY_CEILING = 5;      // UNKNOWN 재조회 상한(초과 시 NEEDS_REVIEW)
    private static final int BASE_BACKOFF_SECONDS = 30; // 30s → 60s → 120s

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankClient bankClient;
    private final BankMaintenancePolicy bankMaintenancePolicy;
    private final ChargeCreditService chargeCreditService;
    private final Clock clock;

    public void process(Long transactionId, long leaseToken) {
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction == null || !transaction.isCharge()) {
            return;
        }

        switch (transaction.getStatus()) {
            case CREATED -> startWithdrawal(transaction, leaseToken);
            case WITHDRAWING -> executeWithdrawal(transaction, leaseToken);
            case WITHDRAWN, DEPOSITING -> credit(transaction, leaseToken);
            case UNKNOWN -> reconcileWithdrawal(transaction, leaseToken);
            case COMPENSATING -> checkCompensationCompletion(transaction, leaseToken);
            default -> { /* terminal 등: no-op */ }
        }
    }

    // CREATED: 실행시점 점검시간 재확인 → write-ahead 의도 → 출금
    private void startWithdrawal(Transaction transaction, long leaseToken) {
        Long id = transaction.getId();
        if (bankMaintenancePolicy.isMaintenanceTime()) {
            // 부작용 없음 → FAILED(BANK_MAINTENANCE)
            transactionRepository.updateStatusFenced(id, TransactionStatus.CREATED,
                    TransactionStatus.FAILED, TransactionFailureReason.BANK_MAINTENANCE, now(), leaseToken);
            return;
        }
        boolean claimed = transactionRepository.updateStatusFenced(id, TransactionStatus.CREATED,
                TransactionStatus.WITHDRAWING, null, null, leaseToken);
        if (!claimed) {
            return;
        }
        transactionRepository.assignExternalIdempotencyKey(id, externalIdempotencyKey(id));
        executeWithdrawal(transaction, leaseToken);
    }

    // WITHDRAWING: 은행 출금(외부). 성공→WITHDRAWN→크레딧 / 4xx→FAILED / 불명→UNKNOWN
    private void executeWithdrawal(Transaction transaction, long leaseToken) {
        Long id = transaction.getId();
        try {
            BankAccount account = bankAccountRepository.findById(transaction.getAccountId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
            bankClient.withdraw(account.getBankCode(), account.getAccountNumber(),
                    transaction.getAmount(), externalIdempotencyKey(id));
        } catch (BankWithdrawalRejectedException e) {
            transactionRepository.updateStatusFenced(id, TransactionStatus.WITHDRAWING,
                    TransactionStatus.FAILED, TransactionFailureReason.WITHDRAWAL_REJECTED, now(), leaseToken);
            return;
        } catch (BankSystemException e) {
            // 결과 불명 → UNKNOWN (대사로 확정). 부작용을 확신할 수 없어 실패 처리하지 않음.
            transactionRepository.updateStatusFenced(id, TransactionStatus.WITHDRAWING,
                    TransactionStatus.UNKNOWN, null, null, leaseToken);
            return;
        }
        transactionRepository.updateStatusFenced(id, TransactionStatus.WITHDRAWING,
                TransactionStatus.WITHDRAWN, null, null, leaseToken);
        credit(transaction, leaseToken);
    }

    // WITHDRAWN/DEPOSITING: 지갑 크레딧. transient→백오프 재시도, 영구(한도)→COMPENSATING(보상은 3D)
    private void credit(Transaction transaction, long leaseToken) {
        Long id = transaction.getId();
        transactionRepository.updateStatusFenced(id, TransactionStatus.WITHDRAWN,
                TransactionStatus.DEPOSITING, null, null, leaseToken);
        try {
            chargeCreditService.credit(id, leaseToken);
        } catch (TransientDataAccessException e) {
            // 락 타임아웃/데드락 등 일시 실패 → 백오프 재시도
            RetryStateDto retry = transactionRepository.findRetryState(id);
            if (retry.getRetryCount() >= retry.getMaxRetries()) {
                initiateCompensation(transaction, leaseToken); // 재시도 소진 → 후진 복구
            } else {
                transactionRepository.scheduleRetry(id, backoffSeconds(retry.getRetryCount()), leaseToken);
            }
        } catch (BusinessException e) {
            // 한도 초과 등 영구 실패 → 즉시 보상 개시(재시도 무의미)
            initiateCompensation(transaction, leaseToken);
        }
    }

    // 후진 복구 개시: DEPOSITING → COMPENSATING + 보상 거래 생성(UNIQUE(parent)로 이중 보상 차단)
    private void initiateCompensation(Transaction charge, long leaseToken) {
        boolean moved = transactionRepository.updateStatusFenced(charge.getId(), TransactionStatus.DEPOSITING,
                TransactionStatus.COMPENSATING, null, null, leaseToken);
        if (!moved) {
            return;
        }
        createCompensationChild(charge);
    }

    private void createCompensationChild(Transaction charge) {
        if (transactionRepository.findByParentTransactionId(charge.getId()).isPresent()) {
            return;
        }
        try {
            transactionRepository.save(com.flab.coongyapay.transaction.domain.Transaction.createCompensation(
                    charge.getWalletId(), charge.getAccountId(), charge.getAmount(), charge.getId()));
        } catch (DuplicateKeyException ignore) {
            // 동시 생성 경합: UNIQUE(parent_transaction_id)가 하나만 허용
        }
    }

    // COMPENSATING 안전망: 자식 보상이 없으면 생성, COMPLETED면 부모를 FAILED(REFUNDED)로 마감
    private void checkCompensationCompletion(Transaction charge, long leaseToken) {
        Optional<Transaction> child = transactionRepository.findByParentTransactionId(charge.getId());
        if (child.isEmpty()) {
            createCompensationChild(charge);
            return;
        }
        if (child.get().getStatus() == TransactionStatus.COMPLETED) {
            transactionRepository.updateStatusFenced(charge.getId(), TransactionStatus.COMPENSATING,
                    TransactionStatus.FAILED, TransactionFailureReason.REFUNDED, now(), leaseToken);
        }
    }

    // UNKNOWN: 은행 대사로 출금 여부 확정
    private void reconcileWithdrawal(Transaction transaction, long leaseToken) {
        Long id = transaction.getId();
        BankWithdrawalStatus status = bankClient.getWithdrawalStatus(externalIdempotencyKey(id));
        switch (status) {
            case WITHDRAWN -> {
                transactionRepository.updateStatusFenced(id, TransactionStatus.UNKNOWN,
                        TransactionStatus.WITHDRAWN, null, null, leaseToken);
                credit(transaction, leaseToken);
            }
            case NOT_WITHDRAWN -> {
                RetryStateDto retry = transactionRepository.findRetryState(id);
                if (isFresh(retry.getFirstAttemptAt())) {
                    // 미출금 확정 + freshness 이내 → 같은 키로 재출금(UNKNOWN → WITHDRAWING)
                    transactionRepository.updateStatusFenced(id, TransactionStatus.UNKNOWN,
                            TransactionStatus.WITHDRAWING, null, null, leaseToken);
                } else {
                    // freshness 초과 → 예상치 못한 시점의 출금 방지 위해 실패 확정
                    transactionRepository.updateStatusFenced(id, TransactionStatus.UNKNOWN,
                            TransactionStatus.FAILED, TransactionFailureReason.WITHDRAWAL_NOT_COMPLETED, now(), leaseToken);
                }
            }
            case UNKNOWN -> {
                // 은행도 불명 → 재조회 카운트. 상한 초과 시 수동 개입(NEEDS_REVIEW), 아니면 백오프.
                transactionRepository.incrementRequeryCount(id, leaseToken);
                RetryStateDto retry = transactionRepository.findRetryState(id);
                if (retry.getRequeryCount() >= REQUERY_CEILING) {
                    transactionRepository.updateStatusFenced(id, TransactionStatus.UNKNOWN,
                            TransactionStatus.NEEDS_REVIEW, null, null, leaseToken);
                } else {
                    transactionRepository.scheduleRetry(id, backoffSeconds(retry.getRetryCount()), leaseToken);
                }
            }
        }
    }

    private boolean isFresh(LocalDateTime firstAttemptAt) {
        if (firstAttemptAt == null) {
            return true;
        }
        return Duration.between(firstAttemptAt, now()).toMinutes() <= FRESHNESS_MINUTES;
    }

    private int backoffSeconds(int retryCount) {
        return BASE_BACKOFF_SECONDS * (1 << Math.min(retryCount, 3)); // 30,60,120,240 cap
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private String externalIdempotencyKey(Long transactionId) {
        return "charge-" + transactionId;
    }
}
