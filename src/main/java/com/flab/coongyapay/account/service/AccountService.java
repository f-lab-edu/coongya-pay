package com.flab.coongyapay.account.service;

import com.flab.coongyapay.account.controller.dto.AccountRegisterRequest;
import com.flab.coongyapay.bank.BankClient;
import com.flab.coongyapay.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final BankClient bankClient;
    private final AccountTransaction accountTransaction;

    public void register(Long userId, AccountRegisterRequest request) throws BusinessException {
        // 1. 은행 API 연동
        bankClient.verify(request.getBankCode(), request.getAccountNumber(), request.getAccountHolderName());

        // 2. 계좌 등록
        accountTransaction.persistRegister(userId, request.getBankCode(), request.getAccountNumber(), request.getAccountHolderName());
    }
    
}
