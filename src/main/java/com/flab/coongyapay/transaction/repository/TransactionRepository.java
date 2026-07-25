package com.flab.coongyapay.transaction.repository;

import com.flab.coongyapay.transaction.assembler.TransactionAssembler;
import com.flab.coongyapay.transaction.domain.Transaction;
import com.flab.coongyapay.transaction.enums.TransactionFailureReason;
import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.mapper.TransactionMapper;
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

    public List<Long> findProcessableChargeIds(int limit) {
        return transactionMapper.findProcessableChargeIds(limit);
    }

    public Optional<Transaction> findChargeByIdAndUserId(Long id, Long userId) {
        return transactionMapper.findChargeByIdAndUserId(id, userId).map(transactionAssembler::toDomain);
    }

    /**
     * CAS 상태 전이. 기대 상태(expected)일 때만 next로 전이하며, 전이된 경우 true.
     * 동시에 다른 워커/대사가 먼저 전이했다면 0행 → false.
     */
    public boolean updateStatus(Long id, TransactionStatus expected, TransactionStatus next,
                                TransactionFailureReason failureReason, LocalDateTime completedAt) {
        return transactionMapper.updateStatus(id, expected.name(), next.name(),
                failureReason == null ? null : failureReason.name(), completedAt) == 1;
    }
}
