package com.flab.coongyapay.idempotency.service;

import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.idempotency.domain.IdempotencyRecord;
import com.flab.coongyapay.idempotency.enums.IdempotencyStatus;
import com.flab.coongyapay.idempotency.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final int RANDOM_UUID_VERSION = 4;

    private final IdempotencyRepository idempotencyRepository;

    public ClaimResult claim(Long userId, String endpoint, String idempotencyKey, String requestHash) {
        IdempotencyRecord record = IdempotencyRecord.create(userId, endpoint, idempotencyKey, requestHash);
        boolean claimed = idempotencyRepository.tryInsertProcessing(record);
        if (claimed) {
            return ClaimResult.newRequest(record.getLeaseToken());
        }

        IdempotencyRecord existingRecord = idempotencyRepository.findByPk(userId, endpoint, idempotencyKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.IDEMPOTENCY_KEY_PROCESSING));

        if (!existingRecord.getRequestHash().equals(requestHash)) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);
        }

        if (IdempotencyStatus.COMPLETED == existingRecord.getStatus()) {
            return ClaimResult.replay(existingRecord.getResponseHttpStatus(), existingRecord.getResponseBody());
        }

        boolean reclaimed = idempotencyRepository.reclaim(userId, endpoint, idempotencyKey);
        if (reclaimed) {
            IdempotencyRecord reclaimedIdempotency = idempotencyRepository.findByPk(userId, endpoint, idempotencyKey)
                    .orElseThrow(() -> new BusinessException(ErrorCode.IDEMPOTENCY_KEY_PROCESSING));
            return ClaimResult.newRequest(reclaimedIdempotency.getLeaseToken());
        } else {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_PROCESSING);
        }
    }

    public void complete(Long userId, String endpoint, String idempotencyKey, int responseHttpStatus, String responseBody, Long leaseToken) {
        boolean completed = idempotencyRepository.complete(userId, endpoint, idempotencyKey, responseHttpStatus, responseBody, leaseToken);
        if (!completed) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_PROCESSING);
        }
    }

    public void release(Long userId, String endpoint, String idempotencyKey, Long leaseToken) {
        idempotencyRepository.release(userId, endpoint, idempotencyKey, leaseToken);
    }

    public void validateIdempotencyKey(String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_IDEMPOTENCY_KEY);
        }
        try {
            if (UUID.fromString(idempotencyKey).version() != RANDOM_UUID_VERSION) {
                throw new BusinessException(ErrorCode.INVALID_IDEMPOTENCY_KEY);
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_IDEMPOTENCY_KEY);
        }
    }
}
