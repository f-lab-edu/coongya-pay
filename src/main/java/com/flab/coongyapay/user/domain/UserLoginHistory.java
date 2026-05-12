package com.flab.coongyapay.user.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserLoginHistory {
    private Long id;
    private final Long userId;
    private boolean success;
    private String failureReason;
    private LocalDateTime loginAt;
    private String ipAddress;

    private UserLoginHistory(Long id, Long userId, boolean success, String failureReason, LocalDateTime loginAt, String ipAddress) {
        this.id = id;
        this.userId = userId;
        this.success = success;
        this.failureReason = failureReason;
        this.loginAt = loginAt;
        this.ipAddress = ipAddress;
    }

    public static UserLoginHistory create(Long userId, boolean success, String failureReason, String ipAddress) {
        return new UserLoginHistory(null, userId, success, failureReason, null, ipAddress);
    }

    public static UserLoginHistory from(Long id, Long userId, boolean success, String failureReason, LocalDateTime loginAt, String ipAddress) {
        return new UserLoginHistory(id, userId, success, failureReason, loginAt, ipAddress);
    }
}
