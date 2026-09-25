package com.flab.coongyapay.charge.worker;

/**
 * 워커가 선점한 거래. leaseToken은 이후 결과 기록 시 fencing(WHERE lease_token=?)에 사용한다.
 */
public record ClaimedTransaction(Long id, long leaseToken) {
}
