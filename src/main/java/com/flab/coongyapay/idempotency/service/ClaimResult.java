package com.flab.coongyapay.idempotency.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClaimResult {

    private final boolean replayed;
    private final Integer responseHttpStatus;
    private final String cachedResponseBody;

    public static ClaimResult newRequest() {
        return new ClaimResult(false, null, null);
    }

    public static ClaimResult replay(int responseHttpStatus, String cachedResponseBody) {
        return new ClaimResult(true, responseHttpStatus, cachedResponseBody);
    }
}
