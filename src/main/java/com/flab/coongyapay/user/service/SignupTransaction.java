package com.flab.coongyapay.user.service;

import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.user.domain.User;
import com.flab.coongyapay.user.domain.UserPassword;
import com.flab.coongyapay.user.domain.UserTransferPin;
import com.flab.coongyapay.user.repository.UserPasswordRepository;
import com.flab.coongyapay.user.repository.UserRepository;
import com.flab.coongyapay.user.repository.UserTransferPinRepository;
import com.flab.coongyapay.wallet.domain.Wallet;
import com.flab.coongyapay.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SignupTransaction {

    private final UserRepository userRepository;
    private final UserPasswordRepository userPasswordRepository;
    private final UserTransferPinRepository userTransferPinRepository;
    private final WalletRepository walletRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void persistSignup(String email, String name, String passwordHash, String transferPinHash) {
        // 1. 이메일 중복 검사(비관적 락)
        Optional<User> existingUser = userRepository.findByEmailForUpdate(email);
        if (existingUser.isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. User 저장
        User user = User.create(email, name);
        User savedUser = userRepository.save(user);

        // 3. UserPassword 저장
        userPasswordRepository.save(UserPassword.create(savedUser.getId(), passwordHash));

        // 4. Wallet 저장
        walletRepository.save(Wallet.create(savedUser.getId()));

        // 5. UserTransferPin 저장
        userTransferPinRepository.save(UserTransferPin.create(savedUser.getId(), transferPinHash));
    }
}
