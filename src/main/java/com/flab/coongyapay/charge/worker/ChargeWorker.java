package com.flab.coongyapay.charge.worker;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 진행 가능한 CHARGE 거래를 주기적으로 선점(lease)·처리하는 내구 워커.
 * 202 응답 이후의 완료 보장을 책임진다.
 *
 * charge.worker.enabled=false 로 비활성화(테스트는 claim/process를 직접 호출).
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "charge.worker.enabled", havingValue = "true", matchIfMissing = true)
public class ChargeWorker {

    private final ChargeDispatcher chargeDispatcher;

    @Scheduled(fixedDelayString = "${charge.worker.fixed-delay-ms:2000}")
    public void poll() {
        chargeDispatcher.runOnce();
    }
}
