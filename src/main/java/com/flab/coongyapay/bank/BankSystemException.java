package com.flab.coongyapay.bank;

/**
 * 은행 응답이 불명확한 경우(timeout/5xx). 출금 여부를 확신할 수 없으므로
 * 워커는 UNKNOWN으로 전이하고 대사(재조회)로 확정한다.
 */
public class BankSystemException extends RuntimeException {
    public BankSystemException(String message) {
        super(message);
    }
}
