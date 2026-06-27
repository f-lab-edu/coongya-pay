package com.flab.coongyapay.account.service;

import com.flab.coongyapay.account.domain.BankAccount;
import com.flab.coongyapay.account.repository.BankAccountRepository;
import com.flab.coongyapay.bank.BankClient;
import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final int MAX_ACCOUNT_LIMIT = 10;

    private final BankAccountRepository bankAccountRepository;
    private final BankClient bankClient;

    @Transactional
    public void register(Long userId, String bankCode, String accountNumber, String accountHolderName) throws BusinessException {
        // 1. 중복 계좌 검증
        boolean exists = bankAccountRepository.existsActiveByUserIdAndAccount(userId, bankCode, accountNumber);
        if (exists) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }

        // 2. 최대 등록 계좌수 검증
        int count = bankAccountRepository.countActiveByUserId(userId);
        if (count >= MAX_ACCOUNT_LIMIT) {
            throw new BusinessException(ErrorCode.ACCOUNT_COUNT_LIMIT_EXCEEDED);
        }

        // 3. 은행 API 연동
        bankClient.verify(bankCode, accountNumber, accountHolderName);

        // 4. 계좌 등록
        bankAccountRepository.save(BankAccount.create(userId, bankCode, accountNumber, accountHolderName));
    }
    
}
