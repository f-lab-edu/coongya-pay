package com.flab.coongyapay.transaction.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private Long walletId;
    private Long accountId;
    private String transactionType;
    private Long parentTransactionId;
    private BigDecimal amount;
    private String status;
    private String remark;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
