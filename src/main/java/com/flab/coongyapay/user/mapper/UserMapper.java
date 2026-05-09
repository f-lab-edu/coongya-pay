package com.flab.coongyapay.user.mapper;

import com.flab.coongyapay.user.mapper.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    boolean existsByEmail(@Param("email") String email);

    Optional<UserDto> findByEmailForUpdate(@Param("email") String email);

    void insert(UserDto userDto);
}
