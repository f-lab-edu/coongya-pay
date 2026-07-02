package com.flab.coongyapay.account.service;

import com.flab.coongyapay.account.domain.BankAccount;
import com.flab.coongyapay.account.repository.BankAccountRepository;
import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static com.flab.coongyapay.account.domain.BankAccount.MAX_ACCOUNT_LIMIT;

@Service
@RequiredArgsConstructor
public class AccountTransaction {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void persistRegister(Long userId, String bankCode, String accountNumber, String accountHolderName) {
        // 1. 사용자 단위 락 확보 - 같은 사용자의 동시 등록 직렬화
        userRepository.findByIdForUpdate(userId);

        // 2. 중복 계좌 검증
        if (bankAccountRepository.existsActiveByUserIdAndAccount(userId, bankCode, accountNumber)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }

        // 3. 최대 등록 계좌수 검증
        int count = bankAccountRepository.countActiveByUserId(userId);
        if (count >= MAX_ACCOUNT_LIMIT) {
            throw new BusinessException(ErrorCode.ACCOUNT_COUNT_LIMIT_EXCEEDED);
        }

        // 4. 계좌 등록
        try {
            bankAccountRepository.save(BankAccount.create(userId, bankCode, accountNumber, accountHolderName));
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }
    }

}
