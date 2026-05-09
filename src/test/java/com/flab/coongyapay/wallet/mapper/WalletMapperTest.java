package com.flab.coongyapay.wallet.mapper;

import com.flab.coongyapay.user.mapper.UserMapper;
import com.flab.coongyapay.user.mapper.dto.UserDto;
import com.flab.coongyapay.wallet.mapper.dto.WalletDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@MybatisTest
class WalletMapperTest {

    @Autowired
    WalletMapper walletMapper;
    @Autowired
    UserMapper userMapper;

    @Test
    void insert_성공하면_id_자동채번() {
        UserDto userDto = new UserDto(null, "user@example.com", "김쿵야");
        userMapper.insert(userDto);

        WalletDto walletDto = new WalletDto(null, userDto.getId(), BigDecimal.ZERO, 0);
        walletMapper.insert(walletDto);
        Assertions.assertThat(walletDto.getId()).isNotNull();
    }
}