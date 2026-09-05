package com.flab.coongyapay.wallet.mapper;

import com.flab.coongyapay.wallet.mapper.dto.WalletDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Optional;

@Mapper
public interface WalletMapper {

    void insert(WalletDto walletDto);

    Optional<WalletDto> findByUserId(@Param("userId") Long userId);

    Optional<WalletDto> findByUserIdForUpdate(@Param("userId") Long userId);

    Optional<WalletDto> findByIdForUpdate(@Param("id") Long id);

    void updateBalanceAndVersion(@Param("id") Long id, @Param("balance") BigDecimal balance, @Param("version") long version);
}
