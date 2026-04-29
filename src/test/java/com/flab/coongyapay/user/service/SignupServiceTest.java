package com.flab.coongyapay.user.service;

import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.kyc.KycClient;
import com.flab.coongyapay.user.controller.dto.SignupRequest;
import com.flab.coongyapay.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @InjectMocks private SignupService signupService;
    @Mock private UserRepository userRepository;
    @Mock private KycClient kycClient;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SignupTransaction signupTransaction;

    @Test
    void 이메일_중복이면_DUPLICATE_EMAIL_던짐() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        Assertions.assertThatThrownBy(() ->
                        signupService.signup(new SignupRequest("user@example.com", "password123!", "김쿵야", "123456")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        verify(kycClient, never()).verify(any());
        verify(passwordEncoder, never()).encode(any());
        verify(signupTransaction, never()).persistSignup(any(), any(), any(), any());
    }
}