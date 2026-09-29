package com.flab.coongyapay.wallet.mapper;

import com.flab.coongyapay.user.mapper.UserMapper;
import com.flab.coongyapay.wallet.mapper.dto.WalletDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;

@MybatisTest
class WalletMapperTest {

    @Autowired
    WalletMapper walletMapper;
    @Autowired
    UserMapper userMapper;

    @Test
    void insert_성공하면_id_자동채번() {
        WalletDto walletDto = getDto();
        walletMapper.insert(walletDto);

        Assertions.assertThat(walletDto.getId()).isNotNull();
    }

    @Test
    void findByUserId_사용자_소유_지갑_반환() {
        WalletDto walletDto = getDto();
        walletMapper.insert(walletDto);
        Optional<WalletDto> optional = walletMapper.findByUserId(walletDto.getUserId());

        Assertions.assertThat(optional.isPresent()).isTrue();
        Assertions.assertThat(optional.get().getUserId()).isEqualTo(walletDto.getUserId());
        Assertions.assertThat(optional.get().getBalance()).isEqualByComparingTo(walletDto.getBalance());
        Assertions.assertThat(optional.get().getVersion()).isEqualTo(walletDto.getVersion());
    }

    @Test
    void findByUserIdForUpdate_사용자_소유_지갑_반환() {
        WalletDto walletDto = getDto();
        walletMapper.insert(walletDto);
        Optional<WalletDto> optional = walletMapper.findByUserIdForUpdate(walletDto.getUserId());

        Assertions.assertThat(optional.isPresent()).isTrue();
        Assertions.assertThat(optional.get().getUserId()).isEqualTo(walletDto.getUserId());
        Assertions.assertThat(optional.get().getBalance()).isEqualByComparingTo(walletDto.getBalance());
        Assertions.assertThat(optional.get().getVersion()).isEqualTo(walletDto.getVersion());
    }

    private static WalletDto getDto() {
        return new WalletDto(null, 1L, BigDecimal.ZERO, 0);
    }
}