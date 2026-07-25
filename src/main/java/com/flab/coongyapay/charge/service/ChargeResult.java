package com.flab.coongyapay.charge.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChargeResult {

    private final Long chargeId;
    private final boolean replayed;
    private final Integer httpStatus;
    private final String responseBody;

    public static ChargeResult fresh(Long chargeId) {
        return new ChargeResult(chargeId, false, null, null);
    }

    public static ChargeResult replay(int httpStatus, String responseBody) {
        return new ChargeResult(null, true, httpStatus, responseBody);
    }
}
