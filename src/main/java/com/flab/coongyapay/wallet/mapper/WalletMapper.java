package com.flab.coongyapay.wallet.mapper;

import com.flab.coongyapay.wallet.mapper.dto.WalletDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletMapper {

    void insert(WalletDto walletDto);
}
