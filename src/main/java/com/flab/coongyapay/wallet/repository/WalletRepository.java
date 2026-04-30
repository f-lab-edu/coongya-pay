package com.flab.coongyapay.wallet.repository;

import com.flab.coongyapay.wallet.domain.Wallet;
import com.flab.coongyapay.wallet.mapper.WalletMapper;
import com.flab.coongyapay.wallet.mapper.dto.WalletDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WalletRepository {

    private final WalletMapper walletMapper;

    public Wallet save(Wallet wallet) {
        WalletDto walletDto = WalletDto.fromDomain(wallet);
        walletMapper.insert(walletDto);
        return walletDto.toDomain();
    }
}
