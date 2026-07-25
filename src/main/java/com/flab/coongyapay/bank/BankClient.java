package com.flab.coongyapay.bank;

import java.math.BigDecimal;

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

    /**
     * 은행 계좌 출금 (외부 API, 비가역). 멱등키(externalIdempotencyKey)로 재시도 시 이중 출금을 방지한다.
     * 성공 시 정상 반환, 명시적 실패는 BusinessException(WITHDRAWAL_REJECTED 등),
     * 결과 불명(timeout/5xx)은 BankSystemException 계열로 구분한다.
     * @param bankCode 은행코드
     * @param accountNumber 출금 계좌번호
     * @param amount 출금액
     * @param externalIdempotencyKey 은행 전송용 멱등키(거래 참조)
     */
    void withdraw(String bankCode, String accountNumber, BigDecimal amount, String externalIdempotencyKey);

    /**
     * 은행 거래 상태 조회 (대사/재조회용). 출금 멱등키(reference)로 실제 출금 여부를 재확인한다.
     * @param externalIdempotencyKey 출금 시 사용한 은행 전송용 멱등키
     * @return 은행이 인지하는 해당 거래의 상태
     */
    BankWithdrawalStatus getWithdrawalStatus(String externalIdempotencyKey);
}