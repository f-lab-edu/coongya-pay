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
public class TransactionEntryDto {
    private Long id;
    private Long transactionId;
    private Long walletId;
    private String entryType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private long walletSequence;
    private LocalDateTime createdAt;
}
