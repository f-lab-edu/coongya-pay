package com.flab.coongyapay.bank;

/**
 * 은행이 인지하는 출금 거래 상태 (대사/재조회 결과).
 */
public enum BankWithdrawalStatus {
    WITHDRAWN,      // 출금 확정됨
    NOT_WITHDRAWN,  // 출금되지 않음(확정)
    UNKNOWN         // 은행도 불명/조회 실패
}
