package com.flab.coongyapay.user.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserTransferPin {
    private final Long id;
    private final Long userId;
    private String transferPinHash;
    private int failedTransferPinCount;
    private LocalDateTime lockedUntil;

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

    //TODO 검증, 변경 메서드 추가하기
    //increaseFailedCount()
    //lockFor()
    //unlock()
    //changeTransferPin()
}
