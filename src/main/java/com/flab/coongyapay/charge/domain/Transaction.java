package com.flab.coongyapay.charge.domain;

import com.flab.coongyapay.charge.enums.TransactionFailureReason;
import com.flab.coongyapay.charge.enums.TransactionStatus;
import com.flab.coongyapay.charge.enums.TransactionType;
import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class Transaction {

    public static final BigDecimal MINIMUM_CHARGE_AMOUNT_LIMIT = BigDecimal.ONE;
    public static final BigDecimal MAXIMUM_CHARGE_AMOUNT_LIMIT = BigDecimal.valueOf(2_000_000);
    public static final int MAXIMUM_REMARK_LENGTH = 7;

    private final Long id;
    private final Long walletId;
    private final TransactionType transactionType;
    private final Long parentTransactionId;
    private final BigDecimal amount;
    private final TransactionStatus status;
    private final String remark;
    private final TransactionFailureReason failureReason;
    private final LocalDateTime createdAt;
    private final LocalDateTime completedAt;

    private Transaction(Long id, Long walletId, TransactionType transactionType, Long parentTransactionId, BigDecimal amount, TransactionStatus status, String remark, TransactionFailureReason failureReason, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.walletId = walletId;
        this.transactionType = transactionType;
        this.parentTransactionId = parentTransactionId;
        this.amount = amount;
        this.status = status;
        this.remark = remark;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public static Transaction createCharge(Long walletId, BigDecimal amount, String remark) {
        if (amount == null || amount.compareTo(MINIMUM_CHARGE_AMOUNT_LIMIT) < 0 || amount.compareTo(MAXIMUM_CHARGE_AMOUNT_LIMIT) > 0) {
            throw new BusinessException(ErrorCode.INVALID_CHARGE_AMOUNT);
        }

        if (remark == null || remark.isBlank() || remark.length() > MAXIMUM_REMARK_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_REMARK_LENGTH);
        }

        return new Transaction(null, walletId, TransactionType.CHARGE, null, amount, TransactionStatus.CREATED, remark, null, null, null);
    }

    public static Transaction from(Long id, Long walletId, TransactionType transactionType, Long parentTransactionId,
                                   BigDecimal amount, TransactionStatus status, String remark,
                                   TransactionFailureReason failureReason, LocalDateTime createdAt,
                                   LocalDateTime completedAt) {
        return new Transaction(id, walletId, transactionType, parentTransactionId, amount, status, remark, failureReason, createdAt, completedAt);
    }
}
