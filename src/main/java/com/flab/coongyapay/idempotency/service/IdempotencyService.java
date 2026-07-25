package com.flab.coongyapay.idempotency.service;

import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.idempotency.domain.IdempotencyRecord;
import com.flab.coongyapay.idempotency.enums.IdempotencyStatus;
import com.flab.coongyapay.idempotency.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;

    public ClaimResult claim(Long userId, String endpoint, String idempotencyKey, String requestHash) {
        IdempotencyRecord record = IdempotencyRecord.create(userId, endpoint, idempotencyKey, requestHash);
        boolean claimed = idempotencyRepository.tryInsertProcessing(record);
        if (claimed) {
            return ClaimResult.newRequest();
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
            return ClaimResult.newRequest();
        } else {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_PROCESSING);
        }
    }

    public void complete(Long userId, String endpoint, String idempotencyKey, int responseHttpStatus, String responseBody) {
        boolean completed = idempotencyRepository.complete(userId, endpoint, idempotencyKey, responseHttpStatus, responseBody);
        if (!completed) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_PROCESSING);
        }
    }

    public void release(Long userId, String endpoint, String idempotencyKey) {
        idempotencyRepository.release(userId, endpoint, idempotencyKey);
    }
}
