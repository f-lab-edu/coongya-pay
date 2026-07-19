package com.flab.coongyapay.bank;

public interface BankClient {

    /**
     * 은행 계좌 연동 (외부 API 연동 인터페이스)
     * @param bankCode
     * @param accountNumber
     * @param accountHolderName
     */
    void verify(String bankCode, String accountNumber, String accountHolderName);

    /**
     * 출금 가능 여부 검증
     * @param bankCode
     * @param accountNumber
     */
    void validateWithdrawal(String bankCode, String accountNumber);
}