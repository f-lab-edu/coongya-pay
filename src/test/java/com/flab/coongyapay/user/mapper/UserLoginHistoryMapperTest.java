package com.flab.coongyapay.user.mapper;

import com.flab.coongyapay.user.mapper.dto.UserLoginHistoryDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

@MybatisTest
class UserLoginHistoryMapperTest {

    @Autowired
    UserLoginHistoryMapper userLoginHistoryMapper;

    @Test
    void insert_성공하면_id_자동채번() {
        UserLoginHistoryDto userLoginHistoryDto = new UserLoginHistoryDto(null, 1L, true, null, null, "address123");
        userLoginHistoryMapper.insert(userLoginHistoryDto);
        Assertions.assertThat(userLoginHistoryDto.getId()).isNotNull();
    }
}