package com.flab.coongyapay.user.mapper;

import com.flab.coongyapay.user.mapper.dto.UserDto;
import com.flab.coongyapay.user.mapper.dto.UserTransferPinDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

@MybatisTest
class UserTransferPinMapperTest {

    @Autowired
    UserTransferPinMapper userTransferPinMapper;
    @Autowired
    UserMapper userMapper;

    @Test
    void insert_성공하면_id_자동채번() {
        UserDto userDto = new UserDto(null, "user@example.com", "김쿵야");
        userMapper.insert(userDto);

        UserTransferPinDto userTransferPinDto = new UserTransferPinDto(null, userDto.getId(), "transferPinHash", 0, null);
        userTransferPinMapper.insert(userTransferPinDto);
        Assertions.assertThat(userTransferPinDto.getId()).isNotNull();
    }

}