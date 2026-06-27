package com.flab.coongyapay.account.mapper;

import com.flab.coongyapay.account.mapper.dto.BankAccountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BankAccountMapper {

    void insert(BankAccountDto bankAccountDto);

    boolean existsActiveByUserIdAndAccount(@Param("userId") Long userId, @Param("bankCode") String bankCode, @Param("accountNumber") String accountNumber);

    int countActiveByUserId(@Param("userId") Long userId);
}
