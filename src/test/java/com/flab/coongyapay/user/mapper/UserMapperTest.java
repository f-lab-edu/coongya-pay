package com.flab.coongyapay.user.mapper;

import com.flab.coongyapay.user.mapper.dto.UserDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@MybatisTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void existsByEmail_이메일_없으면_false() {
        boolean exists = userMapper.existsByEmail("user@example.com");
        Assertions.assertThat(exists).isFalse();
    }

    @Test
    void findByEmailForUpdate_이메일_없으면_Optional_empty() {
        Optional<UserDto> userDtoOptional = userMapper.findByEmailForUpdate("user@example.com");
        Assertions.assertThat(userDtoOptional).isEmpty();
    }

    @Test
    void insert_성공하면_id_자동채번() {
        UserDto userDto = new UserDto(null, "user@example.com", "김쿵야");
        userMapper.insert(userDto);
        Assertions.assertThat(userDto.getId()).isNotNull();
    }

    @Test
    void existsByEmail_이메일_있으면_true() {
        UserDto userDto = new UserDto(null, "user@example.com", "김쿵야");
        userMapper.insert(userDto);
        boolean exists = userMapper.existsByEmail("user@example.com");
        Assertions.assertThat(exists).isTrue();
    }

    @Test
    void findByEmailForUpdate_이메일_있으면_row_반환() {
        UserDto userDto = new UserDto(null, "user@example.com", "김쿵야");
        userMapper.insert(userDto);
        Optional<UserDto> userDtoOptional = userMapper.findByEmailForUpdate(userDto.getEmail());
        Assertions.assertThat(userDtoOptional).isPresent();
        Assertions.assertThat(userDtoOptional.get().getEmail()).isEqualTo(userDto.getEmail());
        Assertions.assertThat(userDtoOptional.get().getName()).isEqualTo(userDto.getName());
    }
}