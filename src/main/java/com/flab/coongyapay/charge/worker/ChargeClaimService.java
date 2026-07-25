package com.flab.coongyapay.charge.worker;

import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.enums.TransactionType;
import com.flab.coongyapay.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * "짧게 선점": FOR UPDATE SKIP LOCKED로 진행 가능한 거래를 골라 즉시 lease를 세팅(짧은 트랜잭션).
 * 트랜잭션 커밋으로 행 락은 풀리지만 lease가 유효한 동안 다른 워커의 재선점을 막는다.
 */
@Service
@RequiredArgsConstructor
public class ChargeClaimService {

    // 3B 해피 경로에서 전진 처리 대상. (UNKNOWN/COMPENSATING 처리는 3C/3D에서 확장)
    private static final List<TransactionStatus> CLAIMABLE_STATUSES = List.of(
            TransactionStatus.CREATED,
            TransactionStatus.WITHDRAWING,
            TransactionStatus.WITHDRAWN,
            TransactionStatus.DEPOSITING,
            TransactionStatus.UNKNOWN);

    private final TransactionRepository transactionRepository;

    @Transactional
    public List<ClaimedTransaction> claimBatch(int limit, int leaseSeconds) {
        List<Long> ids = transactionRepository.selectClaimableIds(TransactionType.CHARGE, CLAIMABLE_STATUSES, limit);
        List<ClaimedTransaction> claimed = new ArrayList<>(ids.size());
        for (Long id : ids) {
            transactionRepository.acquireLease(id, leaseSeconds);
            claimed.add(new ClaimedTransaction(id, transactionRepository.findLeaseToken(id)));
        }
        return claimed;
    }
}
