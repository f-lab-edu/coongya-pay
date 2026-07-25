package com.flab.coongyapay.wallet.repository;

import com.flab.coongyapay.wallet.assembler.WalletAssembler;
import com.flab.coongyapay.wallet.domain.Wallet;
import com.flab.coongyapay.wallet.mapper.WalletMapper;
import com.flab.coongyapay.wallet.mapper.dto.WalletDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WalletRepository {

    private final WalletMapper walletMapper;
    private final WalletAssembler walletAssembler;

    public Wallet save(Wallet wallet) {
        WalletDto walletDto = walletAssembler.toDto(wallet);
        walletMapper.insert(walletDto);
        return walletAssembler.toDomain(walletDto);
    }

    public Optional<Wallet> findByUserId(Long userId) {
        return walletMapper.findByUserId(userId).map(walletAssembler::toDomain);
    }

    public Optional<Wallet> findByUserIdForUpdate(Long userId) {
        return walletMapper.findByUserIdForUpdate(userId).map(walletAssembler::toDomain);
    }
}
