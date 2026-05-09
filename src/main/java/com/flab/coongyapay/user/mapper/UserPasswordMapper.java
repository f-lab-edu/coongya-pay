package com.flab.coongyapay.user.mapper;

import com.flab.coongyapay.user.mapper.dto.UserPasswordDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserPasswordMapper {

    void insert(UserPasswordDto userPasswordDto);
}
