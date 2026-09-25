package com.flab.coongyapay.idempotency.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClaimResult {

    private final boolean replayed;
    private final Integer responseHttpStatus;
    private final String cachedResponseBody;
    private final Long leaseToken;

    public static ClaimResult newRequest(Long leaseToken) {
        return new ClaimResult(false, null, null, leaseToken);
    }

    public static ClaimResult replay(int responseHttpStatus, String cachedResponseBody) {
        return new ClaimResult(true, responseHttpStatus, cachedResponseBody, null);
    }
}
