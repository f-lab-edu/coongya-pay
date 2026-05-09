package com.flab.coongyapay.wallet.domain;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Wallet {
    private final Long id;
    private final Long userId;
    private BigDecimal balance;
    private long version;

    private Wallet(Long id, Long userId, BigDecimal balance, long version) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
        this.version = version;
    }

    public static Wallet create(Long userId) {
        return new Wallet(null, userId, BigDecimal.ZERO, 0);
    }

    public static Wallet from(Long id, Long userId, BigDecimal balance, long version) {
        return new Wallet(id, userId, balance, version);
    }
}
