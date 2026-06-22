package com.flab.coongyapay.auth.service;

import com.flab.coongyapay.user.domain.UserLoginHistory;
import com.flab.coongyapay.user.enums.LoginFailureReason;
import com.flab.coongyapay.user.repository.UserLoginHistoryRepository;
import com.flab.coongyapay.user.repository.UserPasswordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginService {

    private static final int LOCK_THRESHOLD = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(30);

    private final UserPasswordRepository userPasswordRepository;
    private final UserLoginHistoryRepository userLoginHistoryRepository;

    @Transactional
    public void loginFail(Long userId, String ipAddress, LoginFailureReason loginFailureReason) {
        if (LoginFailureReason.ACCOUNT_LOCKED == loginFailureReason) {
            userLoginHistoryRepository.save(UserLoginHistory.failure(userId, loginFailureReason, ipAddress));
            return;
        }

        userPasswordRepository.incrementFailedLoginCount(userId, LOCK_THRESHOLD, LOCK_DURATION);
        userLoginHistoryRepository.save(UserLoginHistory.failure(userId, loginFailureReason, ipAddress));
    }

    @Transactional
    public void loginSuccess(Long userId, String ipAddress) {
        userPasswordRepository.resetFailedLoginCount(userId);
        userLoginHistoryRepository.save(UserLoginHistory.success(userId, ipAddress));
    }
}
