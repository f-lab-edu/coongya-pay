package com.flab.coongyapay.transaction.repository;

import com.flab.coongyapay.transaction.assembler.TransactionAssembler;
import com.flab.coongyapay.transaction.domain.Transaction;
import com.flab.coongyapay.transaction.enums.TransactionFailureReason;
import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.enums.TransactionType;
import com.flab.coongyapay.transaction.mapper.TransactionMapper;
import com.flab.coongyapay.transaction.mapper.dto.RetryStateDto;
import com.flab.coongyapay.transaction.mapper.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionRepository {

    private final TransactionMapper transactionMapper;
    private final TransactionAssembler transactionAssembler;

    public Optional<Transaction> findById(Long id) {
        return transactionMapper.findById(id).map(transactionAssembler::toDomain);
    }

    public Transaction save(Transaction transaction) {
        TransactionDto transactionDto = transactionAssembler.toDto(transaction);
        transactionMapper.insert(transactionDto);
        return transactionAssembler.toDomain(transactionDto);
    }

    public BigDecimal sumInFlightChargeByWalletId(Long walletId) {
        return transactionMapper.sumInFlightChargeByWalletId(walletId);
    }

    public Optional<Transaction> findChargeByIdAndUserId(Long id, Long userId) {
        return transactionMapper.findChargeByIdAndUserId(id, userId).map(transactionAssembler::toDomain);
    }

    public Optional<Transaction> findByParentTransactionId(Long parentTransactionId) {
        return transactionMapper.findByParentTransactionId(parentTransactionId).map(transactionAssembler::toDomain);
    }

    // ===== 비동기 워커: 선점/펜싱/재시도 =====

    public List<Long> selectClaimableIds(TransactionType type, List<TransactionStatus> statuses, int limit) {
        List<String> statusNames = statuses.stream().map(Enum::name).toList();
        return transactionMapper.selectClaimableIds(type.name(), statusNames, limit);
    }

    public void acquireLease(Long id, int leaseSeconds) {
        transactionMapper.acquireLease(id, leaseSeconds);
    }

    public long findLeaseToken(Long id) {
        return transactionMapper.findLeaseToken(id);
    }

    /**
     * CAS 상태 전이(비펜싱). 동기 단계/단일 워커 경로용.
     */
    public boolean updateStatus(Long id, TransactionStatus expected, TransactionStatus next,
                                TransactionFailureReason failureReason, LocalDateTime completedAt) {
        return transactionMapper.updateStatus(id, expected.name(), next.name(),
                failureReason == null ? null : failureReason.name(), completedAt) == 1;
    }

    /**
     * 펜싱 CAS 전이. 선점한 lease_token일 때만 전이 → 재선점된 스테일 워커의 쓰기 차단.
     */
    public boolean updateStatusFenced(Long id, TransactionStatus expected, TransactionStatus next,
                                      TransactionFailureReason failureReason, LocalDateTime completedAt, long leaseToken) {
        return transactionMapper.updateStatusFenced(id, expected.name(), next.name(),
                failureReason == null ? null : failureReason.name(), completedAt, leaseToken) == 1;
    }

    public boolean scheduleRetry(Long id, int backoffSeconds, long leaseToken) {
        return transactionMapper.scheduleRetry(id, backoffSeconds, leaseToken) == 1;
    }

    public boolean incrementRequeryCount(Long id, long leaseToken) {
        return transactionMapper.incrementRequeryCount(id, leaseToken) == 1;
    }

    public RetryStateDto findRetryState(Long id) {
        return transactionMapper.findRetryState(id);
    }

    public void assignExternalIdempotencyKey(Long id, String externalIdempotencyKey) {
        transactionMapper.assignExternalIdempotencyKey(id, externalIdempotencyKey);
    }
}
