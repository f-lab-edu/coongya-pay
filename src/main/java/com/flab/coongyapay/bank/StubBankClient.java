package com.flab.coongyapay.bank;

import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 실제 은행 연동을 대체하는 Stub. 기본 동작은 성공이며, 테스트는 시나리오를 주입해
 * 거절(4xx)/불명(timeout·5xx)/대사 결과를 재현할 수 있다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StubBankClient implements BankClient {

    /** withdraw 시 재현할 결과 */
    public enum WithdrawScenario { SUCCESS, REJECT, UNCLEAR }

    private final BankMaintenancePolicy bankMaintenancePolicy;

    private volatile WithdrawScenario withdrawScenario = WithdrawScenario.SUCCESS;
    private volatile BankWithdrawalStatus withdrawalStatus = BankWithdrawalStatus.WITHDRAWN;
    private volatile WithdrawScenario refundScenario = WithdrawScenario.SUCCESS;
    private volatile BankRefundStatus refundStatus = BankRefundStatus.REFUNDED;

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

    @Override
    public void withdraw(String bankCode, String accountNumber, BigDecimal amount, String externalIdempotencyKey) {
        switch (withdrawScenario) {
            case REJECT -> throw new BankWithdrawalRejectedException(
                    "Withdrawal rejected by bank: key=" + externalIdempotencyKey);
            case UNCLEAR -> throw new BankSystemException(
                    "Withdrawal result unclear (timeout/5xx): key=" + externalIdempotencyKey);
            default -> log.info("Withdraw success: bankCode={}, accountNumber={}, amount={}, key={}",
                    bankCode, accountNumber, amount, externalIdempotencyKey);
        }
    }

    @Override
    public BankWithdrawalStatus getWithdrawalStatus(String externalIdempotencyKey) {
        log.info("Get withdrawal status: key={}, status={}", externalIdempotencyKey, withdrawalStatus);
        return withdrawalStatus;
    }

    @Override
    public void refund(String bankCode, String accountNumber, BigDecimal amount, String externalIdempotencyKey) {
        switch (refundScenario) {
            case REJECT -> throw new BankWithdrawalRejectedException(
                    "Refund rejected by bank: key=" + externalIdempotencyKey);
            case UNCLEAR -> throw new BankSystemException(
                    "Refund result unclear (timeout/5xx): key=" + externalIdempotencyKey);
            default -> log.info("Refund success: bankCode={}, accountNumber={}, amount={}, key={}",
                    bankCode, accountNumber, amount, externalIdempotencyKey);
        }
    }

    @Override
    public BankRefundStatus getRefundStatus(String externalIdempotencyKey) {
        log.info("Get refund status: key={}, status={}", externalIdempotencyKey, refundStatus);
        return refundStatus;
    }

    // --- 테스트 시나리오 주입 ---

    public void setWithdrawScenario(WithdrawScenario scenario) {
        this.withdrawScenario = scenario;
    }

    public void setWithdrawalStatus(BankWithdrawalStatus status) {
        this.withdrawalStatus = status;
    }

    public void setRefundScenario(WithdrawScenario scenario) {
        this.refundScenario = scenario;
    }

    public void setRefundStatus(BankRefundStatus status) {
        this.refundStatus = status;
    }

    public void reset() {
        this.withdrawScenario = WithdrawScenario.SUCCESS;
        this.withdrawalStatus = BankWithdrawalStatus.WITHDRAWN;
        this.refundScenario = WithdrawScenario.SUCCESS;
        this.refundStatus = BankRefundStatus.REFUNDED;
    }
}
