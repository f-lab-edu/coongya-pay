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
 * CHARGE 거래 상태머신을 전진시키는 워커(3A 해피패스).
 * CREATED → WITHDRAWING → (은행 출금) → WITHDRAWN → DEPOSITING → COMPLETED.
 *
 * 핵심 원칙: 은행 출금(withdraw)은 트랜잭션/락 밖에서, 크레딧(원장 기록)은 wallet FOR UPDATE 트랜잭션 안에서.
 * 상태 전이는 CAS(updateStatus)로 처리해 중복 처리를 방지한다.
 * (재시도/UNKNOWN/보상/펜싱은 3B~3D에서 확장)
 */
@Component
@RequiredArgsConstructor
public class ChargeProcessor {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankClient bankClient;
    private final ChargeCreditService chargeCreditService;

    public void process(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction == null || !transaction.isCharge()) {
            return;
        }

        TransactionStatus status = transaction.getStatus();

        // 1. 출금 단계 (CREATED 진입): write-ahead 의도 후 외부 출금 → WITHDRAWN
        if (status == TransactionStatus.CREATED) {
            boolean claimed = transactionRepository.updateStatus(transactionId,
                    TransactionStatus.CREATED, TransactionStatus.WITHDRAWING, null, null);
            if (!claimed) {
                return; // 다른 워커가 선점
            }
            withdraw(transaction);
            transactionRepository.updateStatus(transactionId,
                    TransactionStatus.WITHDRAWING, TransactionStatus.WITHDRAWN, null, null);
            status = TransactionStatus.WITHDRAWN;
        }

        // 2. 크레딧 단계 (WITHDRAWN/DEPOSITING): 지갑 반영 → COMPLETED
        if (status == TransactionStatus.WITHDRAWN || status == TransactionStatus.DEPOSITING) {
            transactionRepository.updateStatus(transactionId,
                    TransactionStatus.WITHDRAWN, TransactionStatus.DEPOSITING, null, null);
            chargeCreditService.credit(transactionId);
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
