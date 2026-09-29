package com.flab.coongyapay.bank;

/**
 * 은행이 인지하는 환불(보상) 거래 상태.
 */
public enum BankRefundStatus {
    REFUNDED,      // 환불 확정됨
    NOT_REFUNDED,  // 환불되지 않음(확정)
    UNKNOWN        // 은행도 불명/조회 실패
}
