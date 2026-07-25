package com.flab.coongyapay.charge.service;

import com.flab.coongyapay.charge.controller.dto.ChargeResponse;
import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.idempotency.service.IdempotencyService;
import com.flab.coongyapay.transaction.domain.Transaction;
import com.flab.coongyapay.transaction.repository.TransactionRepository;
import com.flab.coongyapay.wallet.domain.Wallet;
import com.flab.coongyapay.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ChargeTransaction {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ChargeResult commitReceipt(Long userId, String endpoint, String idempotencyKey, BigDecimal amount, String remark) {
        // 1. wallet 단위 비관적 락
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
        Long walletId = wallet.getId();

        // 2. 처리 중인 충전 거래 포함하여 한도 재검증
        BigDecimal inFlight = transactionRepository.sumInFlightChargeByWalletId(walletId);
        if (!wallet.isChargeableWithin(inFlight, amount)) {
            throw new BusinessException(ErrorCode.WALLET_BALANCE_LIMIT_EXCEEDED);
        }

        // 3. 거래 CREATED 생성
        Transaction savedTransaction = transactionRepository.save(Transaction.createCharge(walletId, amount, remark));

        // 4. 멱등키 COMPLETED + 202 Accepted 응답 캐시
        String body = serialize(ChargeResponse.from(savedTransaction));
        idempotencyService.complete(userId, endpoint, idempotencyKey, HttpStatus.ACCEPTED.value(), body);

        return ChargeResult.fresh(savedTransaction.getId());
    }

    private String serialize(ChargeResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JacksonException e) {
            throw new IllegalStateException(e);
        }
    }
}
