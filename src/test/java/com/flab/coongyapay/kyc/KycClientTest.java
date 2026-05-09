package com.flab.coongyapay.kyc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KycClientTest {

    @Autowired
    private KycClient kycClient;

    @Test
    void verify_Stub_구현체는_항상_성공() {
        kycClient = new StubKycClient();
        Assertions.assertDoesNotThrow(() -> kycClient.verify("test"));
    }
}