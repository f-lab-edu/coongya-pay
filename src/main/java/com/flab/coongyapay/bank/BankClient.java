package com.flab.coongyapay.bank;

public interface BankClient {

    /**
     * 은행 계좌 연동 (외부 API 연동 인터페이스)
     * @param bankCode
     * @param accountNumber
     * @param accountHolderName
     * @throws com.flab.coongyapay.common.exception.BusinessException
     *  - BANK_SYSTEM_UNAVAILABLE: 은행 시스템 장애
     */
    void verify(String bankCode, String accountNumber, String accountHolderName);
}