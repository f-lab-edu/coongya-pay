package com.flab.coongyapay.user.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserTransferPin {
    private static final int MAX_FAILED_TRANSFER_PIN_COUNT = 5;
    public static final LocalDateTime PERMANENT_LOCK = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private final Long id;
    private final Long userId;
    private final String transferPinHash;
    private final int failedTransferPinCount;
    private final LocalDateTime lockedUntil;

    private UserTransferPin(Long id, Long userId, String transferPinHash, int failedTransferPinCount, LocalDateTime lockedUntil) {
        this.id = id;
        this.userId = userId;
        this.transferPinHash = transferPinHash;
        this.failedTransferPinCount = failedTransferPinCount;
        this.lockedUntil = lockedUntil;
    }

    public static UserTransferPin create(Long userId, String transferPinHash) {
        return new UserTransferPin(null, userId, transferPinHash, 0, null);
    }

    public static UserTransferPin from(Long id, Long userId, String transferPinHash, int failedTransferPinCount, LocalDateTime lockedUntil) {
        return new UserTransferPin(id, userId, transferPinHash, failedTransferPinCount, lockedUntil);
    }

    public boolean isLockedAt(LocalDateTime localDateTime) {
        return lockedUntil != null && lockedUntil.isAfter(localDateTime);
    }

    public UserTransferPin incrementFailedTransferPinCount() {
        int newFailedCount = failedTransferPinCount + 1;
        LocalDateTime newLockedUntil = newFailedCount >= MAX_FAILED_TRANSFER_PIN_COUNT ? PERMANENT_LOCK : lockedUntil;
        return from(id, userId, transferPinHash, newFailedCount, newLockedUntil);
    }

    public UserTransferPin resetFailedTransferPinCount() {
        return from(id, userId, transferPinHash, 0, null);
    }

    //TODO 변경 메서드 추가하기
    //changeTransferPin()
}
