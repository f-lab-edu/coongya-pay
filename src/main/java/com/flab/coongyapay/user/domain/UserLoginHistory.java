package com.flab.coongyapay.user.domain;

import com.flab.coongyapay.user.enums.LoginFailureReason;
import com.flab.coongyapay.user.mapper.dto.UserLoginHistoryDto;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserLoginHistory {
    private final Long id;
    private final Long userId;
    private final boolean success;
    private final LoginFailureReason failureReason;
    private final LocalDateTime loginAt;
    private final String ipAddress;

    private UserLoginHistory(Long id, Long userId, boolean success, LoginFailureReason failureReason, LocalDateTime loginAt, String ipAddress) {
        this.id = id;
        this.userId = userId;
        this.success = success;
        this.failureReason = failureReason;
        this.loginAt = loginAt;
        this.ipAddress = ipAddress;
    }

    public static UserLoginHistory success(Long userId, String ipAddress) {
        return new UserLoginHistory(null, userId, true, null, LocalDateTime.now(), ipAddress);
    }

    public static UserLoginHistory failure(Long userId, LoginFailureReason failureReason, String ipAddress) {
        return new UserLoginHistory(null, userId, false, failureReason, LocalDateTime.now(), ipAddress);
    }

    public static UserLoginHistory from(Long id, Long userId, boolean success, LoginFailureReason failureReason, LocalDateTime loginAt, String ipAddress) {
        return new UserLoginHistory(id, userId, success, failureReason, loginAt, ipAddress);
    }

}
