package com.flab.coongyapay.charge.service;

import com.flab.coongyapay.account.domain.BankAccount;
import com.flab.coongyapay.account.repository.BankAccountRepository;
import com.flab.coongyapay.bank.BankClient;
import com.flab.coongyapay.bank.BankMaintenancePolicy;
import com.flab.coongyapay.charge.controller.dto.ChargeRequest;
import com.flab.coongyapay.charge.controller.dto.ChargeStatusResponse;
import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.common.exception.ErrorResponse;
import com.flab.coongyapay.common.util.RequestHashUtil;
import com.flab.coongyapay.idempotency.service.ClaimResult;
import com.flab.coongyapay.idempotency.service.IdempotencyService;
import com.flab.coongyapay.transaction.domain.Transaction;
import com.flab.coongyapay.transaction.repository.TransactionRepository;
import com.flab.coongyapay.user.service.UserTransferPinVerifier;
import com.flab.coongyapay.wallet.domain.Wallet;
import com.flab.coongyapay.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ChargeService {
    private static final String DELIMITER = "|";
    private static final String ENDPOINT = "POST /api/v1/charges";
    private static final int MAX_REMARK_LENGTH = 7;

    private final IdempotencyService idempotencyService;
    private final BankMaintenancePolicy bankMaintenancePolicy;
    private final BankAccountRepository bankAccountRepository;
    private final WalletRepository walletRepository;
    private final UserTransferPinVerifier userTransferPinVerifier;
    private final BankClient bankClient;
    private final ChargeTransaction chargeTransaction;
    private final ObjectMapper objectMapper;
    private final TransactionRepository transactionRepository;

    public ChargeResult charge(Long userId, String userName, String idempotencyKey, ChargeRequest request) {
        // 1. 멱등키 선점
        String requestHash = RequestHashUtil.sha256Hex(canonicalize(request));
        ClaimResult claimed = idempotencyService.claim(userId, ENDPOINT, idempotencyKey, requestHash);
        if (claimed.isReplayed()) {
            return ChargeResult.replay(claimed.getResponseHttpStatus(), claimed.getCachedResponseBody());
        }

        // 2. 사전 검증
        BankAccount bankAccount;
        try {
            // 2.1. 은행 점검 시간 검증
            if (bankMaintenancePolicy.isMaintenanceTime()) {
                throw new BusinessException(ErrorCode.BANK_MAINTENANCE);
            }

            // 2.2. 계좌 소유 및 활성 여부 검증
            bankAccount = bankAccountRepository.findActiveByIdAndUserId(request.getBankAccountId(), userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

            // 2.3. 지갑 잔액 한도 검증
            Wallet wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

            if (!wallet.isChargeableWithin(BigDecimal.ZERO, request.getAmount())) {
                throw new BusinessException(ErrorCode.WALLET_BALANCE_LIMIT_EXCEEDED);
            }

            // 2.4. 송금비밀번호 검증
            userTransferPinVerifier.verify(userId, request.getTransferPin());
        } catch (BusinessException e) {
            // 사전 검증 실패 시 멱등키 release
            idempotencyService.release(userId, ENDPOINT, idempotencyKey);
            throw e;
        }

        // 3. 은행 출금 사전 검증
        try {
            bankClient.validateWithdrawal(bankAccount.getBankCode(), bankAccount.getAccountNumber());
        } catch (BusinessException e) {
            cacheFailure(userId, idempotencyKey, e);
            throw e;
        }

        // 4. 충전 접수 커밋
        try {
            String remark = resolveRemark(request.getRemark(), userName);
            return chargeTransaction.commitReceipt(userId, ENDPOINT, idempotencyKey, bankAccount.getId(), request.getAmount(), remark);
        } catch (BusinessException e) {
            cacheFailure(userId, idempotencyKey, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ChargeStatusResponse getCharge(Long userId, Long chargeId) {
        Transaction chargeTransaction = transactionRepository.findChargeByIdAndUserId(chargeId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

        return ChargeStatusResponse.from(chargeTransaction);
    }

    private String canonicalize(ChargeRequest request) {
        return request.getBankAccountId() + DELIMITER
                + request.getAmount().stripTrailingZeros().toPlainString() + DELIMITER
                + (request.getRemark() == null ? "" : request.getRemark());
    }

    private void cacheFailure(Long userId, String idempotencyKey, BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        String body = serialize(ErrorResponse.of(errorCode));
        idempotencyService.complete(userId, ENDPOINT, idempotencyKey, errorCode.getHttpStatus().value(), body);
    }

    private String serialize(ErrorResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JacksonException e) {
            throw new IllegalStateException(e);
        }
    }

    private String resolveRemark(String remark, String userName) {
        if (remark == null || remark.isBlank()) {
            return userName.substring(0, Math.min(userName.length(), MAX_REMARK_LENGTH));
        }

        return remark;
    }
}
