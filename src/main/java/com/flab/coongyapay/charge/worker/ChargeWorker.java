package com.flab.coongyapay.charge.worker;

import com.flab.coongyapay.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 진행 가능한 CHARGE 거래를 주기적으로 폴링해 상태머신을 전진시키는 스케줄 워커.
 * 202 응답의 내구성은 이 워커가 보장한다(@Async fast-path는 3B에서 추가).
 *
 * charge.worker.enabled=false 로 비활성화 가능(테스트는 process()를 직접 호출).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "charge.worker.enabled", havingValue = "true", matchIfMissing = true)
public class ChargeWorker {

    private static final int BATCH_SIZE = 50;

    private final TransactionRepository transactionRepository;
    private final ChargeProcessor chargeProcessor;

    @Scheduled(fixedDelayString = "${charge.worker.fixed-delay-ms:2000}")
    public void poll() {
        List<Long> ids = transactionRepository.findProcessableChargeIds(BATCH_SIZE);
        for (Long id : ids) {
            try {
                chargeProcessor.process(id);
            } catch (Exception e) {
                log.warn("Charge processing failed: transactionId={}", id, e);
            }
        }
    }
}
