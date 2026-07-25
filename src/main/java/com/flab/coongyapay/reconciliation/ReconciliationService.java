package com.flab.coongyapay.reconciliation;

import com.flab.coongyapay.reconciliation.mapper.ReconciliationMapper;
import com.flab.coongyapay.transaction.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 대사·정합성·모니터링. 스케줄로 주기 실행되며, 수렴이 멈춘 지점을 탐지·알림한다.
 * charge.worker.enabled=false 로 스케줄 비활성화(테스트는 메서드를 직접 호출하거나 매퍼 단위테스트).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "charge.worker.enabled", havingValue = "true", matchIfMissing = true)
public class ReconciliationService {

    private static final int IDLE_EXPIRE_SECONDS = 600;       // CREATED 유휴 만료 임계(10분)
    private static final int NON_TERMINAL_DWELL_MINUTES = 5;  // 체류 경보 임계

    private final ReconciliationMapper reconciliationMapper;

    // 유휴 CREATED(부작용 없음) 만료. WITHDRAWING/UNKNOWN은 만료 대상 아님(출금 위험).
    @Scheduled(fixedDelayString = "${reconciliation.expire-delay-ms:60000}")
    public void expireIdleCreated() {
        int expired = reconciliationMapper.expireIdleCreatedCharges(IDLE_EXPIRE_SECONDS);
        if (expired > 0) {
            log.info("Reconciliation: expired {} idle CREATED charges", expired);
        }
    }

    // 내부 3-테이블 불변식 검증 (위반=인시던트)
    @Scheduled(fixedDelayString = "${reconciliation.invariant-delay-ms:300000}")
    public void verifyInvariants() {
        int balanceMismatch = reconciliationMapper.countBalanceMismatches();
        int versionViolation = reconciliationMapper.countVersionIntegrityViolations();
        int creditViolation = reconciliationMapper.countCompletedChargesWithoutSingleCredit();
        if (balanceMismatch > 0 || versionViolation > 0 || creditViolation > 0) {
            log.error("Reconciliation INVARIANT VIOLATION: balanceMismatch={}, versionViolation={}, completedChargeCreditViolation={}",
                    balanceMismatch, versionViolation, creditViolation);
        }
    }

    // 수렴 정체 모니터링: NEEDS_REVIEW(0이 정상), 장기 체류, UNKNOWN 적체
    @Scheduled(fixedDelayString = "${reconciliation.monitor-delay-ms:60000}")
    public void reportMetrics() {
        int needsReview = reconciliationMapper.countByStatus(TransactionStatus.NEEDS_REVIEW.name());
        int unknown = reconciliationMapper.countByStatus(TransactionStatus.UNKNOWN.name());
        int stuck = reconciliationMapper.countNonTerminalOlderThanMinutes(NON_TERMINAL_DWELL_MINUTES);
        if (needsReview > 0) {
            log.error("Monitoring: NEEDS_REVIEW={} (수동 개입 필요)", needsReview);
        }
        if (stuck > 0 || unknown > 0) {
            log.warn("Monitoring: non-terminal dwell(>{}m)={}, UNKNOWN={}", NON_TERMINAL_DWELL_MINUTES, stuck, unknown);
        }
    }
}
