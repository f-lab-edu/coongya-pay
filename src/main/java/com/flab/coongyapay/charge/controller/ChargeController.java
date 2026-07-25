package com.flab.coongyapay.charge.controller;

import com.flab.coongyapay.auth.userdetails.CustomUserDetails;
import com.flab.coongyapay.charge.controller.dto.ChargeRequest;
import com.flab.coongyapay.charge.controller.dto.ChargeResponse;
import com.flab.coongyapay.charge.controller.dto.ChargeStatusResponse;
import com.flab.coongyapay.charge.service.ChargeResult;
import com.flab.coongyapay.charge.service.ChargeService;
import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.transaction.enums.TransactionStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChargeController {

    private final ChargeService chargeService;

    @PostMapping("/api/v1/charges")
    public ResponseEntity<?> charge(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ChargeRequest request) {

        // 1. 멱등키 형식 검증
        validateIdempotencyKey(idempotencyKey);

        // 2. 충전 서비스 호출
        ChargeResult result = chargeService.charge(userDetails.getUser().getId(), userDetails.getUser().getName(), idempotencyKey, request);

        // 3. 서비스 호출 결과에 따른 응답 변환
        if (result.isReplayed()) {
            return ResponseEntity.status(result.getHttpStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result.getResponseBody());
        }

        ChargeResponse responseBody = new ChargeResponse(result.getChargeId(), TransactionStatus.CREATED.name());
        return ResponseEntity.accepted()
                .location(URI.create("/api/v1/charges/" + result.getChargeId()))
                .body(responseBody);
    }

    @GetMapping("/api/v1/charges/{chargeId}")
    public ChargeStatusResponse getCharge(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long chargeId) {
        return chargeService.getCharge(userDetails.getUser().getId(), chargeId);
    }

    private void validateIdempotencyKey(String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_IDEMPOTENCY_KEY);
        }
        try {
            if (UUID.fromString(idempotencyKey).version() != 4) {
                throw new BusinessException(ErrorCode.INVALID_IDEMPOTENCY_KEY);
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_IDEMPOTENCY_KEY);
        }
    }
}
