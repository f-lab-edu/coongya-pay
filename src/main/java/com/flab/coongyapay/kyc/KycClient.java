package com.flab.coongyapay.kyc;

public interface KycClient {

    /**
     * 본인 인증 (외부 API 연동 인터페이스)
     *
     * @param name
     * @throws com.flab.coongyapay.common.exception.BusinessException
     *  - KYC_VERIFICATION_FAILED: 본인 인증 실패
     *  - KYC_SERVICE_UNAVAILABLE: KYC 서버 에러
     */
    void verify(String name);
}
