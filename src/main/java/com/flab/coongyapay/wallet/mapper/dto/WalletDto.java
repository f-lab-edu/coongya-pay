package com.flab.coongyapay.wallet.mapper.dto;

import com.flab.coongyapay.wallet.domain.Wallet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private long version;

    public static WalletDto fromDomain(Wallet wallet) {
        return new WalletDto(wallet.getId(), wallet.getUserId(), wallet.getBalance(), wallet.getVersion());
    }

    public Wallet toDomain() {
        return Wallet.from(this.id, this.userId, this.balance, this.version);
    }
}
