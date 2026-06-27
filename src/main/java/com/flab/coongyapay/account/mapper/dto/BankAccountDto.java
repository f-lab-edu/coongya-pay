package com.flab.coongyapay.account.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDto {
    private Long id;
    private Long userId;
    private String bankCode;
    private String accountNumber;
    private String accountHolderName;
    private LocalDateTime registeredAt;
    private LocalDateTime deletedAt;
}
