package com.flab.coongyapay.user.service;

import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.user.domain.UserTransferPin;
import com.flab.coongyapay.user.repository.UserTransferPinRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTransferPinVerifierTest {

    @InjectMocks
    private UserTransferPinVerifier userTransferPinVerifier;

    @Mock
    private UserTransferPinRepository userTransferPinRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void 송금비밀번호_검증_성공시_실패카운트_잠금시각_리셋() {
        when(userTransferPinRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(UserTransferPin.from(1L, 1L, "hash", 3, null)));
        when(passwordEncoder.matches("111111", "hash")).thenReturn(true);

        userTransferPinVerifier.verify(1L, "111111");

        ArgumentCaptor<UserTransferPin> userTransferPinArgumentCaptor = ArgumentCaptor.forClass(UserTransferPin.class);
        verify(userTransferPinRepository).update(userTransferPinArgumentCaptor.capture());
        Assertions.assertThat(userTransferPinArgumentCaptor.getValue().getFailedTransferPinCount()).isZero();
        Assertions.assertThat(userTransferPinArgumentCaptor.getValue().getLockedUntil()).isNull();
        Assertions.assertThat(userTransferPinArgumentCaptor.getValue().isLockedAt(LocalDateTime.now())).isFalse();
    }

    @Test
    void 송금비밀번호_틀리면_INVALID_TRANSFER_PIN_던짐() {
        when(userTransferPinRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(UserTransferPin.from(1L, 1L, "hash", 0, null)));
        when(passwordEncoder.matches("111111", "hash")).thenReturn(false);

        Assertions.assertThatThrownBy(() -> {
                    userTransferPinVerifier.verify(1L, "111111");
                }).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TRANSFER_PIN);

        ArgumentCaptor<UserTransferPin> userTransferPinArgumentCaptor = ArgumentCaptor.forClass(UserTransferPin.class);
        verify(userTransferPinRepository).update(userTransferPinArgumentCaptor.capture());
        Assertions.assertThat(userTransferPinArgumentCaptor.getValue().getFailedTransferPinCount()).isEqualTo(1);
    }

    @Test
    void 송금비밀번호_오류_5회_도달시_잠금_후_TRANSFER_PIN_LOCKED_던짐() {
        when(userTransferPinRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(UserTransferPin.from(1L, 1L, "hash", 4, null)));
        when(passwordEncoder.matches("111111", "hash")).thenReturn(false);

        Assertions.assertThatThrownBy(() -> {
                    userTransferPinVerifier.verify(1L, "111111");
                }).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TRANSFER_PIN_LOCKED);

        ArgumentCaptor<UserTransferPin> userTransferPinArgumentCaptor = ArgumentCaptor.forClass(UserTransferPin.class);
        verify(userTransferPinRepository).update(userTransferPinArgumentCaptor.capture());
        Assertions.assertThat(userTransferPinArgumentCaptor.getValue().getFailedTransferPinCount()).isEqualTo(5);
        Assertions.assertThat(userTransferPinArgumentCaptor.getValue().getLockedUntil()).isNotNull();
        Assertions.assertThat(userTransferPinArgumentCaptor.getValue().isLockedAt(LocalDateTime.now())).isTrue();
    }

    @Test
    void 송금비밀번호_잠겨있으면_TRANSFER_PIN_LOCKED_던짐() {
        when(userTransferPinRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(UserTransferPin.from(1L, 1L, "hash", 5, UserTransferPin.PERMANENT_LOCK)));

        Assertions.assertThatThrownBy(() -> {
                    userTransferPinVerifier.verify(1L, "111111");
                }).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TRANSFER_PIN_LOCKED);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userTransferPinRepository, never()).update(any());
    }

}