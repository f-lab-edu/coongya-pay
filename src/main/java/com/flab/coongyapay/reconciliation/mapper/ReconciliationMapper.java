package com.flab.coongyapay.reconciliation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReconciliationMapper {

    // 유휴 CREATED(부작용 없음) → EXPIRED. WITHDRAWING/UNKNOWN은 절대 만료하지 않음(출금 위험).
    int expireIdleCreatedCharges(@Param("idleSeconds") int idleSeconds);

    // 내부 정합성 불변식 위반 건수 (0이 정상)
    int countBalanceMismatches();

    int countVersionIntegrityViolations();

    int countCompletedChargesWithoutSingleCredit();

    // 모니터링 지표
    int countNonTerminalOlderThanMinutes(@Param("minutes") int minutes);

    int countByStatus(@Param("status") String status);
}
