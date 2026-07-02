package com.flab.coongyapay.bank;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StubBankClient implements BankClient {

    @Override
    public void verify(String bankCode, String accountNumber, String accountHolderName) {
        log.info("Bank account verified: bank code={}, account number={}, account holder name={}", bankCode, accountNumber, accountHolderName);
    }
}
