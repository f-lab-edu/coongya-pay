package com.flab.coongyapay.wallet.domain;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Wallet {
    public static final BigDecimal MAXIMUM_BALANCE_LIMIT = BigDecimal.valueOf(2_000_000);

    private final Long id;
    private final Long userId;
    private final BigDecimal balance;
    private final long version;

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

    public boolean isChargeableWithin(BigDecimal inFlight, BigDecimal amount) {
        return balance.add(inFlight).add(amount).compareTo(MAXIMUM_BALANCE_LIMIT) <= 0;
    }
}
