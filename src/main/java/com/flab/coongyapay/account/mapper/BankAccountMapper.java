package com.flab.coongyapay.account.mapper;

import com.flab.coongyapay.account.mapper.dto.BankAccountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BankAccountMapper {

    void insert(BankAccountDto bankAccountDto);

    int countActiveByUserId(@Param("userId") Long userId);

    Optional<BankAccountDto> findActiveByUserIdAndAccountForUpdate(@Param("userId") Long userId, @Param("bankCode") String bankCode, @Param("accountNumber") String accountNumber);

    List<BankAccountDto> findActiveByUserId(@Param("userId") Long userId);
}
