package com.flab.coongyapay.user.mapper;

import com.flab.coongyapay.user.mapper.dto.UserDto;
import com.flab.coongyapay.user.mapper.dto.UserPasswordDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

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

    @Test
    void findByUserId_조회() {
        UserDto userDto = new UserDto(null, "user@example.com", "김쿵야");
        userMapper.insert(userDto);

        UserPasswordDto userPasswordDto = new UserPasswordDto(null, userDto.getId(), "passwordHash", 0, null);
        userPasswordMapper.insert(userPasswordDto);

        Optional<UserPasswordDto> foundUserPasswordDto = userPasswordMapper.findByUserId(userDto.getId());
        Assertions.assertThat(foundUserPasswordDto).isPresent();
        Assertions.assertThat(foundUserPasswordDto.get().getUserId()).isEqualTo(userDto.getId());
        Assertions.assertThat(foundUserPasswordDto.get().getPassword()).isEqualTo("passwordHash");
        Assertions.assertThat(foundUserPasswordDto.get().getFailedLoginCount()).isZero();
        Assertions.assertThat(foundUserPasswordDto.get().getLockedUntil()).isNull();
    }

    @Test
    void incrementFailedLoginCount_5회_미만시_카운트_증가() {
        UserDto userDto = new UserDto(null, "user@example.com", "김쿵야");
        userMapper.insert(userDto);

        UserPasswordDto userPasswordDto = new UserPasswordDto(null, userDto.getId(), "passwordHash", 3, null);
        userPasswordMapper.insert(userPasswordDto);

        userPasswordMapper.incrementFailedLoginCount(userDto.getId(), 5, 30L);

        Optional<UserPasswordDto> foundByUserId = userPasswordMapper.findByUserId(userDto.getId());
        Assertions.assertThat(foundByUserId.get().getFailedLoginCount()).isEqualTo(4);
        Assertions.assertThat(foundByUserId.get().getLockedUntil()).isNull();
    }

    @Test
    void incrementFailedLoginCount_5회_도달시_계정_잠금() {
        UserDto userDto = new UserDto(null, "user@example.com", "김쿵야");
        userMapper.insert(userDto);

        UserPasswordDto userPasswordDto = new UserPasswordDto(null, userDto.getId(), "passwordHash", 4, null);
        userPasswordMapper.insert(userPasswordDto);

        userPasswordMapper.incrementFailedLoginCount(userDto.getId(), 5, 30L);

        Optional<UserPasswordDto> foundByUserId = userPasswordMapper.findByUserId(userDto.getId());
        Assertions.assertThat(foundByUserId.get().getFailedLoginCount()).isEqualTo(5);
        Assertions.assertThat(foundByUserId.get().getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    void incrementFailedLoginCount_잠금만료시_카운트_1로_리셋_및_잠금시간_리셋() {
        UserDto userDto = new UserDto(null, "user@example.com", "김쿵야");
        userMapper.insert(userDto);

        UserPasswordDto userPasswordDto = new UserPasswordDto(null, userDto.getId(), "passwordHash", 5, LocalDateTime.now().minusMinutes(1));
        userPasswordMapper.insert(userPasswordDto);

        userPasswordMapper.incrementFailedLoginCount(userDto.getId(), 5, 30L);

        Optional<UserPasswordDto> foundByUserId = userPasswordMapper.findByUserId(userDto.getId());
        Assertions.assertThat(foundByUserId.get().getFailedLoginCount()).isEqualTo(1);
        Assertions.assertThat(foundByUserId.get().getLockedUntil()).isNull();
    }

    @Test
    void resetFailedLoginCount_카운트_0_리셋_및_잠금시간_리셋() {
        UserDto userDto = new UserDto(null, "user@example.com", "김쿵야");
        userMapper.insert(userDto);

        UserPasswordDto userPasswordDto = new UserPasswordDto(null, userDto.getId(), "passwordHash", 5, LocalDateTime.now().minusMinutes(1));
        userPasswordMapper.insert(userPasswordDto);

        userPasswordMapper.resetFailedLoginCount(userDto.getId());

        Optional<UserPasswordDto> foundByUserId = userPasswordMapper.findByUserId(userDto.getId());
        Assertions.assertThat(foundByUserId.get().getFailedLoginCount()).isZero();
        Assertions.assertThat(foundByUserId.get().getLockedUntil()).isNull();
    }
}