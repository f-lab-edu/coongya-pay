package com.flab.coongyapay.idempotency.domain;

import com.flab.coongyapay.idempotency.enums.IdempotencyStatus;
import lombok.Getter;

@Getter
public class IdempotencyRecord {
    private final Long userId;
    private final String endpoint;
    private final String idempotencyKey;
    private final String requestHash;
    private final IdempotencyStatus status;
    private final Integer responseHttpStatus;
    private final String responseBody;

    private IdempotencyRecord(Long userId, String endpoint, String idempotencyKey, String requestHash, IdempotencyStatus status, Integer responseHttpStatus, String responseBody) {
        this.userId = userId;
        this.endpoint = endpoint;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.status = status;
        this.responseHttpStatus = responseHttpStatus;
        this.responseBody = responseBody;
    }

    public static IdempotencyRecord create(Long userId, String endpoint, String idempotencyKey, String requestHash) {
        return new IdempotencyRecord(userId, endpoint, idempotencyKey, requestHash, IdempotencyStatus.PROCESSING, null, null);
    }

    public static IdempotencyRecord from(Long userId, String endpoint, String idempotencyKey, String requestHash, IdempotencyStatus status, Integer responseHttpStatus, String responseBody) {
        return new IdempotencyRecord(userId, endpoint, idempotencyKey, requestHash, status, responseHttpStatus, responseBody);
    }
}
