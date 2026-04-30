package com.flab.coongyapay.user.mapper;

import com.flab.coongyapay.user.mapper.dto.UserDto;
import com.flab.coongyapay.user.mapper.dto.UserPasswordDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

@MybatisTest
class UserPasswordMapperTest {

    @Autowired
    UserPasswordMapper userPasswordMapper;
    @Autowired
    UserMapper userMapper;

    @Test
    void insert_성공하면_id_자동채번() {
        UserDto userDto = new UserDto(null, "user@example.com", "김쿵야");
        userMapper.insert(userDto);

        UserPasswordDto userPasswordDto = new UserPasswordDto(null, userDto.getId(), "passwordHash", 0, null);
        userPasswordMapper.insert(userPasswordDto);
        Assertions.assertThat(userPasswordDto.getId()).isNotNull();
    }
}