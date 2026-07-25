package com.flab.coongyapay.transaction.repository;

import com.flab.coongyapay.transaction.assembler.TransactionAssembler;
import com.flab.coongyapay.transaction.domain.Transaction;
import com.flab.coongyapay.transaction.mapper.TransactionMapper;
import com.flab.coongyapay.transaction.mapper.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
}
