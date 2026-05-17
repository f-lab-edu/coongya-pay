package com.flab.coongyapay.kyc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KycClientTest {

    @Test
    void verify_Stub_구현체는_항상_성공() {
        KycClient kycClient = new StubKycClient();
        Assertions.assertDoesNotThrow(() -> kycClient.verify("test"));
    }
}