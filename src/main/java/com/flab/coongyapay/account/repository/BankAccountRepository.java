package com.flab.coongyapay.account.repository;

import com.flab.coongyapay.account.assembler.BankAccountAssembler;
import com.flab.coongyapay.account.domain.BankAccount;
import com.flab.coongyapay.account.mapper.BankAccountMapper;
import com.flab.coongyapay.account.mapper.dto.BankAccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BankAccountRepository {

    private final BankAccountMapper bankAccountMapper;
    private final BankAccountAssembler bankAccountAssembler;

    public BankAccount save(BankAccount bankAccount) {
        BankAccountDto dto = bankAccountAssembler.toDto(bankAccount);
        bankAccountMapper.insert(dto);
        return bankAccountAssembler.toDomain(dto);
    }

    public boolean existsActiveByUserIdAndAccount(Long userId, String bankCode, String accountNumber) {
        return bankAccountMapper.existsActiveByUserIdAndAccount(userId, bankCode, accountNumber);
    }

    public int countActiveByUserId(Long userId) {
        return bankAccountMapper.countActiveByUserId(userId);
    }
}
