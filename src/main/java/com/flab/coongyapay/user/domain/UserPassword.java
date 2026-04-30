package com.flab.coongyapay.user.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserPassword {
    private final Long id;
    private final Long userId;
    private String passwordHash;
    private int failedLoginCount;
    private LocalDateTime lockedUntil;

    private UserPassword(Long id, Long userId, String passwordHash, int failedLoginCount, LocalDateTime lockedUntil) {
        this.id = id;
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.failedLoginCount = failedLoginCount;
        this.lockedUntil = lockedUntil;
    }

    public static UserPassword create(Long userId, String passwordHash) {
        return new UserPassword(null, userId, passwordHash, 0, null);
    }

    public static UserPassword from(Long id, Long userId, String passwordHash, int failedLoginCount, LocalDateTime lockedUntil) {
        return new UserPassword(id, userId, passwordHash, failedLoginCount, lockedUntil);
    }

    //TODO 인증,회원정보변경 메서드 추가하기
    //increaseFailedCount()
    //lockFor()
    //unlock()
    //changePassword()
}
