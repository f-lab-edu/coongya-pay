package com.flab.coongyapay.user.mapper;

import com.flab.coongyapay.user.mapper.dto.UserLoginHistoryDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserLoginHistoryMapper {

    void insert(UserLoginHistoryDto userLoginHistoryDto);
}
