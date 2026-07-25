package com.flab.coongyapay.transaction.assembler;

import com.flab.coongyapay.transaction.domain.TransactionEntry;
import com.flab.coongyapay.transaction.enums.TransactionEntryType;
import com.flab.coongyapay.transaction.mapper.dto.TransactionEntryDto;
import org.springframework.stereotype.Component;

@Component
public class TransactionEntryAssembler {

    public TransactionEntryDto toDto(TransactionEntry domain) {
        return new TransactionEntryDto(
                domain.getId(),
                domain.getTransactionId(),
                domain.getWalletId(),
                domain.getEntryType().name(),
                domain.getAmount(),
                domain.getBalanceAfter(),
                domain.getWalletSequence(),
                domain.getCreatedAt()
        );
    }

    public TransactionEntry toDomain(TransactionEntryDto dto) {
        return TransactionEntry.from(
                dto.getId(),
                dto.getTransactionId(),
                dto.getWalletId(),
                TransactionEntryType.valueOf(dto.getEntryType()),
                dto.getAmount(),
                dto.getBalanceAfter(),
                dto.getWalletSequence(),
                dto.getCreatedAt()
        );
    }
}
