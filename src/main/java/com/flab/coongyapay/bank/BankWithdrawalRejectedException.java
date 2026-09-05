package com.flab.coongyapay.bank;

/**
 * 은행이 출금을 명시적으로 거절(4xx)한 경우. 재시도해도 결과가 바뀌지 않으므로
 * 워커는 재시도 없이 FAILED(WITHDRAWAL_REJECTED)로 전이한다.
 */
public class BankWithdrawalRejectedException extends RuntimeException {
    public BankWithdrawalRejectedException(String message) {
        super(message);
    }
}
