package com.flab.coongyapay.idempotency.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyRecordDto {
    private Long userId;
    private String endpoint;
    private String idempotencyKey;
    private String requestHash;
    private String status;
    private Integer responseHttpStatus;
    private String responseBody;
    private LocalDateTime leaseExpiresAt;
    private LocalDateTime expiresAt;
}
