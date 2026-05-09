package com.flab.coongyapay.wallet.assembler;

import com.flab.coongyapay.wallet.domain.Wallet;
import com.flab.coongyapay.wallet.mapper.dto.WalletDto;
import org.springframework.stereotype.Component;

@Component
public class WalletAssembler {

    public WalletDto toDto(Wallet domain) {
        return new WalletDto(
                domain.getId(),
                domain.getUserId(),
                domain.getBalance(),
                domain.getVersion()
        );
    }

    public Wallet toDomain(WalletDto dto) {
        return Wallet.from(
                dto.getId(),
                dto.getUserId(),
                dto.getBalance(),
                dto.getVersion()
        );
    }
}
