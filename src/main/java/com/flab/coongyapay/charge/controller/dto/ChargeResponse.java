package com.flab.coongyapay.charge.controller.dto;

import com.flab.coongyapay.transaction.domain.Transaction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChargeResponse {

    private final Long chargeId;
    private final String status;

    public static ChargeResponse from(Transaction transaction) {
        return new ChargeResponse(transaction.getId(), transaction.getStatus().name());
    }
}
