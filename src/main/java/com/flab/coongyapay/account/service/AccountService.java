package com.flab.coongyapay.account.service;

import com.flab.coongyapay.account.controller.dto.AccountRegisterRequest;
import com.flab.coongyapay.account.controller.dto.AccountResponse;
import com.flab.coongyapay.account.repository.BankAccountRepository;
import com.flab.coongyapay.bank.BankClient;
import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.flab.coongyapay.account.domain.BankAccount.MAX_ACCOUNT_LIMIT;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final BankClient bankClient;
    private final AccountTransaction accountTransaction;
    private final BankAccountRepository bankAccountRepository;

    public void register(Long userId, AccountRegisterRequest request) {
        // 1. 중복 계좌 사전 검증
        if (bankAccountRepository.existsActiveByUserIdAndAccount(userId, request.getBankCode(), request.getAccountNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }

        // 2. 최대 등록 계좌수 사전 검증
        int count = bankAccountRepository.countActiveByUserId(userId);
        if (count >= MAX_ACCOUNT_LIMIT) {
            throw new BusinessException(ErrorCode.ACCOUNT_COUNT_LIMIT_EXCEEDED);
        }

        // 3. 은행 API 연동
        bankClient.verify(request.getBankCode(), request.getAccountNumber(), request.getAccountHolderName());

        // 4. 계좌 등록
        accountTransaction.persistRegister(userId, request.getBankCode(), request.getAccountNumber(), request.getAccountHolderName());
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccounts(Long userId) {
        return bankAccountRepository.findActiveByUserId(userId).stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id, Long userId) {
        int affected = bankAccountRepository.softDelete(id, userId);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
    }
}
