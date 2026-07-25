package com.flab.coongyapay.transaction.repository;

import com.flab.coongyapay.transaction.assembler.TransactionEntryAssembler;
import com.flab.coongyapay.transaction.domain.TransactionEntry;
import com.flab.coongyapay.transaction.enums.TransactionEntryType;
import com.flab.coongyapay.transaction.mapper.TransactionEntryMapper;
import com.flab.coongyapay.transaction.mapper.dto.TransactionEntryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionEntryRepository {

    private final TransactionEntryMapper transactionEntryMapper;
    private final TransactionEntryAssembler transactionEntryAssembler;

    public TransactionEntry save(TransactionEntry entry) {
        TransactionEntryDto dto = transactionEntryAssembler.toDto(entry);
        transactionEntryMapper.insert(dto);
        return transactionEntryAssembler.toDomain(dto);
    }

    public Optional<TransactionEntry> findByTransactionIdAndEntryType(Long transactionId, TransactionEntryType entryType) {
        return transactionEntryMapper.findByTransactionIdAndEntryType(transactionId, entryType.name())
                .map(transactionEntryAssembler::toDomain);
    }

    public boolean existsCredit(Long transactionId) {
        return transactionEntryMapper.findByTransactionIdAndEntryType(transactionId, TransactionEntryType.CREDIT.name())
                .isPresent();
    }
}
