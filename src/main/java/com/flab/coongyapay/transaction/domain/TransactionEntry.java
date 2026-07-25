package com.flab.coongyapay.transaction.domain;

import com.flab.coongyapay.transaction.enums.TransactionEntryType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 지갑 원장(ledger) 항목. 잔액을 직접 UPDATE 하지 않고 CREDIT/DEBIT 항목을 append 하여
 * 이력을 남긴다. (transaction_id, entry_type) UNIQUE로 이중 기입을 DB가 차단하고,
 * (wallet_id, wallet_sequence) UNIQUE로 지갑별 순번 무결성을 보장한다.
 */
@Getter
public class TransactionEntry {

    private final Long id;
    private final Long transactionId;
    private final Long walletId;
    private final TransactionEntryType entryType;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final long walletSequence;
    private final LocalDateTime createdAt;

    private TransactionEntry(Long id, Long transactionId, Long walletId, TransactionEntryType entryType,
                            BigDecimal amount, BigDecimal balanceAfter, long walletSequence, LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.entryType = entryType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.walletSequence = walletSequence;
        this.createdAt = createdAt;
    }

    public static TransactionEntry credit(Long transactionId, Long walletId, BigDecimal amount,
                                          BigDecimal balanceAfter, long walletSequence) {
        return new TransactionEntry(null, transactionId, walletId, TransactionEntryType.CREDIT,
                amount, balanceAfter, walletSequence, null);
    }

    public static TransactionEntry from(Long id, Long transactionId, Long walletId, TransactionEntryType entryType,
                                        BigDecimal amount, BigDecimal balanceAfter, long walletSequence, LocalDateTime createdAt) {
        return new TransactionEntry(id, transactionId, walletId, entryType, amount, balanceAfter, walletSequence, createdAt);
    }
}
