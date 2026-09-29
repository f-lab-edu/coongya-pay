package com.flab.coongyapay.charge.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 보상(환불) 거래를 주기적으로 선점·처리하는 내구 워커. CHARGE 워커와 동일 인프라 재사용.
 * charge.worker.enabled=false 로 비활성화(테스트는 claim/process를 직접 호출).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "charge.worker.enabled", havingValue = "true", matchIfMissing = true)
public class CompensationWorker {

    private static final int BATCH_SIZE = 50;
    private static final int LEASE_SECONDS = 30;

    private final CompensationClaimService compensationClaimService;
    private final CompensationProcessor compensationProcessor;

    @Scheduled(fixedDelayString = "${charge.worker.fixed-delay-ms:2000}")
    public void poll() {
        List<ClaimedTransaction> claimed = compensationClaimService.claimBatch(BATCH_SIZE, LEASE_SECONDS);
        for (ClaimedTransaction transaction : claimed) {
            try {
                compensationProcessor.process(transaction.id(), transaction.leaseToken());
            } catch (Exception e) {
                log.warn("Compensation processing failed: transactionId={}", transaction.id(), e);
            }
        }
    }
}
