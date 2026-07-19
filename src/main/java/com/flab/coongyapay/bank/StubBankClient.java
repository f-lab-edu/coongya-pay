package com.flab.coongyapay.bank;

import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StubBankClient implements BankClient {

    private final BankMaintenancePolicy bankMaintenancePolicy;

    @Override
    public void verify(String bankCode, String accountNumber, String accountHolderName) {
        log.info("Bank account verified: bank code={}, account number={}, account holder name={}", bankCode, accountNumber, accountHolderName);
    }

    @Override
    public void validateWithdrawal(String bankCode, String accountNumber) {
        if (bankMaintenancePolicy.isMaintenanceTime()) {
            throw new BusinessException(ErrorCode.BANK_MAINTENANCE);
        }
        log.info("Validate withdrawal: bankCode={}, account number={}", bankCode, accountNumber);
    }
}
