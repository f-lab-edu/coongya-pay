package com.flab.coongyapay.transaction.assembler;

import com.flab.coongyapay.transaction.domain.Transaction;
import com.flab.coongyapay.transaction.enums.TransactionFailureReason;
import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.enums.TransactionType;
import com.flab.coongyapay.transaction.mapper.dto.TransactionDto;
import org.springframework.stereotype.Component;

@Component
public class TransactionAssembler {

    public TransactionDto toDto(Transaction domain) {
        return new TransactionDto(domain.getId(), domain.getWalletId(), domain.getAccountId(), domain.getTransactionType().name(), domain.getParentTransactionId(), domain.getAmount(), domain.getStatus().name(), domain.getRemark(), domain.getFailureReason() == null ? null : domain.getFailureReason().name(), domain.getCreatedAt(), domain.getCompletedAt());
    }

    public Transaction toDomain(TransactionDto dto) {
        return Transaction.from(dto.getId(), dto.getWalletId(), dto.getAccountId(), TransactionType.valueOf(dto.getTransactionType()), dto.getParentTransactionId(), dto.getAmount(), TransactionStatus.valueOf(dto.getStatus()), dto.getRemark(), dto.getFailureReason() == null ? null : TransactionFailureReason.valueOf(dto.getFailureReason()), dto.getCreatedAt(), dto.getCompletedAt());
    }
}
