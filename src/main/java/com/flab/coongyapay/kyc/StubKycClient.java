package com.flab.coongyapay.kyc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StubKycClient implements KycClient {

    @Override
    public void verify(String name) {
        log.info("KYC Verified: name={}", name);
    }
}
