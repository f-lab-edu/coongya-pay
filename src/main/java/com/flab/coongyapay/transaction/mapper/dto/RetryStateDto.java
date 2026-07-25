package com.flab.coongyapay.transaction.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 워커 재시도/대사 판정에 필요한 거래의 진행 상태 스냅샷.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetryStateDto {
    private int retryCount;
    private int maxRetries;
    private int requeryCount;
    private LocalDateTime firstAttemptAt;
    private String externalIdempotencyKey;
}
