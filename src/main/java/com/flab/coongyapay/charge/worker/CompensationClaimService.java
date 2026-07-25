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
 * 보상(COMPENSATION) 거래 선점. CHARGE 워커와 동일한 lease/fencing 인프라를 재사용한다.
 */
@Service
@RequiredArgsConstructor
public class CompensationClaimService {

    private static final List<TransactionStatus> CLAIMABLE_STATUSES = List.of(
            TransactionStatus.CREATED,
            TransactionStatus.REFUNDING,
            TransactionStatus.UNKNOWN);

    private final TransactionRepository transactionRepository;

    @Transactional
    public List<ClaimedTransaction> claimBatch(int limit, int leaseSeconds) {
        List<Long> ids = transactionRepository.selectClaimableIds(TransactionType.COMPENSATION, CLAIMABLE_STATUSES, limit);
        List<ClaimedTransaction> claimed = new ArrayList<>(ids.size());
        for (Long id : ids) {
            transactionRepository.acquireLease(id, leaseSeconds);
            claimed.add(new ClaimedTransaction(id, transactionRepository.findLeaseToken(id)));
        }
        return claimed;
    }
}
