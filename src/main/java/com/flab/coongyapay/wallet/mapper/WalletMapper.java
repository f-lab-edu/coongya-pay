package com.flab.coongyapay.wallet.mapper;

import com.flab.coongyapay.wallet.mapper.dto.WalletDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface WalletMapper {

    void insert(WalletDto walletDto);

    Optional<WalletDto> findByUserId(@Param("userId") Long userId);

    Optional<WalletDto> findByUserIdForUpdate(@Param("userId") Long userId);
}
