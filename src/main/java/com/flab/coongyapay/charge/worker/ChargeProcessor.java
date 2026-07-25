package com.flab.coongyapay.charge.worker;

import com.flab.coongyapay.account.domain.BankAccount;
import com.flab.coongyapay.account.repository.BankAccountRepository;
import com.flab.coongyapay.bank.BankClient;
import com.flab.coongyapay.charge.service.ChargeCreditService;
import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.transaction.domain.Transaction;
import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * CHARGE 거래 상태머신을 전진시키는 워커.
 * CREATED → WITHDRAWING → (은행 출금) → WITHDRAWN → DEPOSITING → COMPLETED.
 *
 * 원칙: 은행 출금(withdraw)은 트랜잭션/락 밖, 크레딧(원장)은 wallet FOR UPDATE 트랜잭션 안.
 * 모든 상태 기록은 선점한 leaseToken으로 fencing → 리스 만료 후 재선점된 스테일 워커의 쓰기 차단.
 * (실패/UNKNOWN/보상은 3C/3D에서 확장)
 */
@Component
@RequiredArgsConstructor
public class ChargeProcessor {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankClient bankClient;
    private final ChargeCreditService chargeCreditService;

    public void process(Long transactionId, long leaseToken) {
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction == null || !transaction.isCharge()) {
            return;
        }

        TransactionStatus status = transaction.getStatus();

        // 1. write-ahead 출금 의도 (CREATED → WITHDRAWING) + 외부 멱등키 확정
        if (status == TransactionStatus.CREATED) {
            boolean claimed = transactionRepository.updateStatusFenced(transactionId,
                    TransactionStatus.CREATED, TransactionStatus.WITHDRAWING, null, null, leaseToken);
            if (!claimed) {
                return; // 펜싱: 다른 워커가 선점/전이
            }
            transactionRepository.assignExternalIdempotencyKey(transactionId, externalIdempotencyKey(transactionId));
            status = TransactionStatus.WITHDRAWING;
        }

        // 2. 은행 출금 (외부, 락/트랜잭션 밖) → WITHDRAWN
        if (status == TransactionStatus.WITHDRAWING) {
            withdraw(transaction);
            transactionRepository.updateStatusFenced(transactionId,
                    TransactionStatus.WITHDRAWING, TransactionStatus.WITHDRAWN, null, null, leaseToken);
            status = TransactionStatus.WITHDRAWN;
        }

        // 3. 크레딧 (WITHDRAWN → DEPOSITING → COMPLETED)
        if (status == TransactionStatus.WITHDRAWN || status == TransactionStatus.DEPOSITING) {
            transactionRepository.updateStatusFenced(transactionId,
                    TransactionStatus.WITHDRAWN, TransactionStatus.DEPOSITING, null, null, leaseToken);
            chargeCreditService.credit(transactionId, leaseToken);
        }
    }

    private void withdraw(Transaction transaction) {
        BankAccount account = bankAccountRepository.findById(transaction.getAccountId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
        bankClient.withdraw(account.getBankCode(), account.getAccountNumber(),
                transaction.getAmount(), externalIdempotencyKey(transaction.getId()));
    }

    private String externalIdempotencyKey(Long transactionId) {
        return "charge-" + transactionId;
    }
}
