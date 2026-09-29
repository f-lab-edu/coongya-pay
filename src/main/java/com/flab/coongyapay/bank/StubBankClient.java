package com.flab.coongyapay.bank;

import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 실제 은행 연동을 대체하는 Stub.
 * 기본 동작은 성공이다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StubBankClient implements BankClient {

    private final BankMaintenancePolicy bankMaintenancePolicy;

    @Override
    public void verify(String bankCode, String accountNumber, String accountHolderName) {
        log.info("Bank account verified");
    }

    @Override
    public void validateWithdrawal(String bankCode, String accountNumber) {
        if (bankMaintenancePolicy.isMaintenanceTime()) {
            throw new BusinessException(ErrorCode.BANK_MAINTENANCE);
        }
        log.info("Validate withdrawal");
    }

    @Override
    public void withdraw(String bankCode, String accountNumber, BigDecimal amount, String externalIdempotencyKey) {
        log.info("Withdraw success: amount={}, key={}", amount, externalIdempotencyKey);
    }

    @Override
    public BankWithdrawalStatus getWithdrawalStatus(String externalIdempotencyKey) {
        log.info("Get withdrawal status: key={}, status={}", externalIdempotencyKey, BankWithdrawalStatus.WITHDRAWN);
        return BankWithdrawalStatus.WITHDRAWN;
    }

    @Override
    public void refund(String bankCode, String accountNumber, BigDecimal amount, String externalIdempotencyKey) {
        log.info("Refund success: amount={}, key={}", amount, externalIdempotencyKey);
    }

    @Override
    public BankRefundStatus getRefundStatus(String externalIdempotencyKey) {
        log.info("Get refund status: key={}, status={}", externalIdempotencyKey, BankRefundStatus.REFUNDED);
        return BankRefundStatus.REFUNDED;
    }

}
