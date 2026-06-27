package com.flab.coongyapay.account.service;

import com.flab.coongyapay.account.domain.BankAccount;
import com.flab.coongyapay.account.repository.BankAccountRepository;
import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountTransaction {

    private static final int MAX_ACCOUNT_LIMIT = 10; // 등록 가능한 활성 계좌수

    private final BankAccountRepository bankAccountRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void persistRegister(Long userId, String bankCode, String accountNumber, String accountHolderName) {
        // 1. 중복 계좌 검증
        Optional<BankAccount> existingAccount = bankAccountRepository.findActiveByUserIdAndAccountForUpdate(userId, bankCode, accountNumber);
        if (existingAccount.isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }

        // 2. 최대 등록 계좌수 검증
        int count = bankAccountRepository.countActiveByUserId(userId);
        if (count >= MAX_ACCOUNT_LIMIT) {
            throw new BusinessException(ErrorCode.ACCOUNT_COUNT_LIMIT_EXCEEDED);
        }

        // 3. 계좌 등록
        bankAccountRepository.save(BankAccount.create(userId, bankCode, accountNumber, accountHolderName));
    }

}
