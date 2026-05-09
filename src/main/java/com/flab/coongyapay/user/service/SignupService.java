package com.flab.coongyapay.user.service;

import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.kyc.KycClient;
import com.flab.coongyapay.user.controller.dto.SignupRequest;
import com.flab.coongyapay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final UserRepository userRepository;
    private final KycClient kycClient;
    private final PasswordEncoder passwordEncoder;
    private final SignupTransaction signupTransaction;

    public void signup(SignupRequest request) {

        // 1. 이메일 중복 검사
        boolean exists = userRepository.existsByEmail(request.getEmail());
        if (exists) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. KYC
        kycClient.verify(request.getName());

        // 3. 비밀번호 단방향 해시
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // 4. 송금 비밀번호 단방향 해시
        String transferPinHash = passwordEncoder.encode(request.getTransferPin());

        // 5. 회원가입 정보 저장
        signupTransaction.persistSignup(request.getEmail(), request.getName(), passwordHash, transferPinHash);
    }
}
