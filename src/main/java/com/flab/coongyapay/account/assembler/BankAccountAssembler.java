package com.flab.coongyapay.account.assembler;

import com.flab.coongyapay.account.domain.BankAccount;
import com.flab.coongyapay.account.mapper.dto.BankAccountDto;
import org.springframework.stereotype.Component;

@Component
public class BankAccountAssembler {

    public BankAccountDto toDto(BankAccount domain) {
        return new BankAccountDto(
                domain.getId(),
                domain.getUserId(),
                domain.getBankCode(),
                domain.getAccountNumber(),
                domain.getAccountHolderName(),
                domain.getRegisteredAt(),
                domain.getDeletedAt()
        );
    }

    public BankAccount toDomain(BankAccountDto dto) {
        return BankAccount.from(
                dto.getId(),
                dto.getUserId(),
                dto.getBankCode(),
                dto.getAccountNumber(),
                dto.getAccountHolderName(),
                dto.getRegisteredAt(),
                dto.getDeletedAt()
        );
    }
}
