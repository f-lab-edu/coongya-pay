package com.flab.coongyapay.user.service;

import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.user.domain.UserTransferPin;
import com.flab.coongyapay.user.repository.UserTransferPinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserTransferPinVerifier {

    private final UserTransferPinRepository userTransferPinRepository;
    private final PasswordEncoder passwordEncoder;

    // 송금 비밀번호 검증 실패로 예외 발생해도 실패 카운트 및 잠금 UPDATE를 커밋하기 위해 noRollbackFor를 추가
    @Transactional(noRollbackFor = BusinessException.class)
    public void verify(Long userId, String rawPin) {
        // 1. UserTransferPin 행 단위 비관적 락
        UserTransferPin userTransferPin = userTransferPinRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        // 2. 잠금 여부 확인
        if (userTransferPin.isLockedAt(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.TRANSFER_PIN_LOCKED);
        }

        // 3. BCrypt 비교
        //     - 실패 시 failedTransferPinCount 증가, 5회 도달 시 잠금
        //     - 성공 시 failedTransferPinCount 리셋, lockedUntil 리셋
        if (!passwordEncoder.matches(rawPin, userTransferPin.getTransferPinHash())) {
            UserTransferPin incremented = userTransferPin.incrementFailedTransferPinCount();
            userTransferPinRepository.update(incremented);

            if (incremented.isLockedAt(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.TRANSFER_PIN_LOCKED);
            }

            throw new BusinessException(ErrorCode.INVALID_TRANSFER_PIN);
        }

        UserTransferPin reset = userTransferPin.resetFailedTransferPinCount();
        userTransferPinRepository.update(reset);
    }
}
