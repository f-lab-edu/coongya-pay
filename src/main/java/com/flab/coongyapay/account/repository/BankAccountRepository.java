package com.flab.coongyapay.account.repository;

import com.flab.coongyapay.account.assembler.BankAccountAssembler;
import com.flab.coongyapay.account.domain.BankAccount;
import com.flab.coongyapay.account.mapper.BankAccountMapper;
import com.flab.coongyapay.account.mapper.dto.BankAccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public int countActiveByUserId(Long userId) {
        return bankAccountMapper.countActiveByUserId(userId);
    }

    public Optional<BankAccount> findActiveByUserIdAndAccountForUpdate(Long userId, String bankCode, String accountNumber) {
        return bankAccountMapper.findActiveByUserIdAndAccountForUpdate(userId, bankCode, accountNumber).map(bankAccountAssembler::toDomain);
    }

    public List<BankAccount> findActiveByUserId(Long userId) {
        return bankAccountMapper.findActiveByUserId(userId).stream()
                .map(bankAccountAssembler::toDomain)
                .collect(Collectors.toList());
    }

    public Optional<BankAccount> findActiveByIdAndUserId(Long id, Long userId) {
        return bankAccountMapper.findActiveByIdAndUserId(id, userId).map(bankAccountAssembler::toDomain);
    }

    public void softDelete(Long id, Long userId) {
        bankAccountMapper.softDelete(id, userId);
    }
}
