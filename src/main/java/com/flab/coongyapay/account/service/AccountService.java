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

@Service
@RequiredArgsConstructor
public class AccountService {

    private final BankClient bankClient;
    private final AccountTransaction accountTransaction;
    private final BankAccountRepository bankAccountRepository;

    public void register(Long userId, AccountRegisterRequest request) throws BusinessException {
        // 1. 은행 API 연동
        bankClient.verify(request.getBankCode(), request.getAccountNumber(), request.getAccountHolderName());

        // 2. 계좌 등록
        accountTransaction.persistRegister(userId, request.getBankCode(), request.getAccountNumber(), request.getAccountHolderName());
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccounts(Long userId) {
        return bankAccountRepository.findActiveByUserId(userId).stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAccount(Long id, Long userId) {
        // 1. 삭제 대상 계좌 조회
        bankAccountRepository.findActiveByIdAndUserId(id, userId).orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        // 2. 계좌 삭제
        bankAccountRepository.softDelete(id, userId);
    }
}
