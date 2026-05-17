package com.flab.coongyapay.user.mapper;

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

    @Test
    void insert_성공하면_id_자동채번() {
        UserPasswordDto userPasswordDto = new UserPasswordDto(null, 1L, "passwordHash", 0, null);
        userPasswordMapper.insert(userPasswordDto);
        Assertions.assertThat(userPasswordDto.getId()).isNotNull();
    }

    @Test
    void findByUserId_조회() {
        UserPasswordDto userPasswordDto = new UserPasswordDto(null, 1L, "passwordHash", 0, null);
        userPasswordMapper.insert(userPasswordDto);

        Optional<UserPasswordDto> foundUserPasswordDto = userPasswordMapper.findByUserId(1L);
        Assertions.assertThat(foundUserPasswordDto).isPresent();
        Assertions.assertThat(foundUserPasswordDto.get().getUserId()).isEqualTo(1L);
        Assertions.assertThat(foundUserPasswordDto.get().getPassword()).isEqualTo("passwordHash");
        Assertions.assertThat(foundUserPasswordDto.get().getFailedLoginCount()).isZero();
        Assertions.assertThat(foundUserPasswordDto.get().getLockedUntil()).isNull();
    }

    @Test
    void incrementFailedLoginCount_5회_미만시_카운트_증가() {
        UserPasswordDto userPasswordDto = new UserPasswordDto(null, 1L, "passwordHash", 3, null);
        userPasswordMapper.insert(userPasswordDto);

        userPasswordMapper.incrementFailedLoginCount(1L, 5, 30L);

        Optional<UserPasswordDto> foundByUserId = userPasswordMapper.findByUserId(1L);
        Assertions.assertThat(foundByUserId).isPresent();
        Assertions.assertThat(foundByUserId.get().getFailedLoginCount()).isEqualTo(4);
        Assertions.assertThat(foundByUserId.get().getLockedUntil()).isNull();
    }

    @Test
    void incrementFailedLoginCount_5회_도달시_계정_잠금() {
        UserPasswordDto userPasswordDto = new UserPasswordDto(null, 1L, "passwordHash", 4, null);
        userPasswordMapper.insert(userPasswordDto);

        userPasswordMapper.incrementFailedLoginCount(1L, 5, 30L);

        Optional<UserPasswordDto> foundByUserId = userPasswordMapper.findByUserId(1L);
        Assertions.assertThat(foundByUserId).isPresent();
        Assertions.assertThat(foundByUserId.get().getFailedLoginCount()).isEqualTo(5);
        Assertions.assertThat(foundByUserId.get().getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    void incrementFailedLoginCount_잠금만료시_카운트_및_잠금시간_리셋_후_재카운팅() {
        UserPasswordDto userPasswordDto = new UserPasswordDto(null, 1L, "passwordHash", 5, LocalDateTime.now().minusMinutes(1));
        userPasswordMapper.insert(userPasswordDto);

        userPasswordMapper.incrementFailedLoginCount(1L, 5, 30L);

        Optional<UserPasswordDto> foundByUserId = userPasswordMapper.findByUserId(1L);
        Assertions.assertThat(foundByUserId).isPresent();
        Assertions.assertThat(foundByUserId.get().getFailedLoginCount()).isEqualTo(1);
        Assertions.assertThat(foundByUserId.get().getLockedUntil()).isNull();
    }

    @Test
    void resetFailedLoginCount_카운트_및_잠금시간_리셋() {
        UserPasswordDto userPasswordDto = new UserPasswordDto(null, 1L, "passwordHash", 5, LocalDateTime.now().minusMinutes(1));
        userPasswordMapper.insert(userPasswordDto);

        userPasswordMapper.resetFailedLoginCount(1L);

        Optional<UserPasswordDto> foundByUserId = userPasswordMapper.findByUserId(1L);
        Assertions.assertThat(foundByUserId).isPresent();
        Assertions.assertThat(foundByUserId.get().getFailedLoginCount()).isZero();
        Assertions.assertThat(foundByUserId.get().getLockedUntil()).isNull();
    }
}