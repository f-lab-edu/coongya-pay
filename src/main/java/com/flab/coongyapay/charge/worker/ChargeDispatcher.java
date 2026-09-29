package com.flab.coongyapay.charge.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 선점→처리 한 사이클을 실행하는 공용 디스패처.
 * - 내구 경로: {@link ChargeWorker}가 @Scheduled로 runOnce() 호출
 * - fast-path: 커밋 직후 {@link #dispatchAsync()}로 즉시 처리(@Async, best-effort)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChargeDispatcher {

    private static final int BATCH_SIZE = 50;
    private static final int LEASE_SECONDS = 30;

    private final ChargeClaimService chargeClaimService;
    private final ChargeProcessor chargeProcessor;

    public void runOnce() {
        List<ClaimedTransaction> claimed = chargeClaimService.claimBatch(BATCH_SIZE, LEASE_SECONDS);
        for (ClaimedTransaction transaction : claimed) {
            try {
                chargeProcessor.process(transaction.id(), transaction.leaseToken());
            } catch (Exception e) {
                // 예외 시 lease 만료 후 재선점되어 재처리(내구성). 실패 유형별 전이는 3C에서 정교화.
                log.warn("Charge processing failed: transactionId={}", transaction.id(), e);
            }
        }
    }

    /** 커밋 직후 호출되는 fast-path. 실패해도 스케줄 워커가 결국 처리하므로 best-effort. */
    @Async
    public void dispatchAsync() {
        try {
            runOnce();
        } catch (Exception e) {
            log.warn("Async charge dispatch failed", e);
        }
    }
}
