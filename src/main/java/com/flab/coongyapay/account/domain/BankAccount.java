package com.flab.coongyapay.account.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BankAccount {
    private final Long id;
    private final Long userId;
    private final String bankCode;
    private final String accountNumber;
    private final String accountHolderName;
    private final LocalDateTime registeredAt;
    private final LocalDateTime deletedAt;

    private BankAccount(Long id, Long userId, String bankCode, String accountNumber, String accountHolderName, LocalDateTime registeredAt, LocalDateTime deletedAt) {
        this.id = id;
        this.userId = userId;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.registeredAt = registeredAt;
        this.deletedAt = deletedAt;
    }

    public static BankAccount create(Long userId, String bankCode, String accountNumber, String accountHolderName) {
        return new BankAccount(null, userId, bankCode, accountNumber, accountHolderName, null, null);
    }

    public static BankAccount from(Long id, Long userId, String bankCode, String accountNumber, String accountHolderName, LocalDateTime registeredAt, LocalDateTime deletedAt) {
        return new BankAccount(id, userId, bankCode, accountNumber, accountHolderName, registeredAt, deletedAt);
    }

}
