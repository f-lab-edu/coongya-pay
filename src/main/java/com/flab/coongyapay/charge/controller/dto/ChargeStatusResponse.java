package com.flab.coongyapay.charge.controller.dto;

import com.flab.coongyapay.transaction.domain.Transaction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ChargeStatusResponse {
    private final Long chargeId;
    private final String status;
    private final BigDecimal amount;
    private final String remark;
    private final LocalDateTime createdAt;
    private final LocalDateTime completedAt;

    public static ChargeStatusResponse from(Transaction transaction) {
        return new ChargeStatusResponse(transaction.getId(), transaction.getStatus().name(), transaction.getAmount(), transaction.getRemark(), transaction.getCreatedAt(), transaction.getCompletedAt());
    }
}
