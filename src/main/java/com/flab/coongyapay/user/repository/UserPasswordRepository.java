package com.flab.coongyapay.user.repository;

import com.flab.coongyapay.user.domain.UserPassword;
import com.flab.coongyapay.user.mapper.UserPasswordMapper;
import com.flab.coongyapay.user.mapper.dto.UserPasswordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserPasswordRepository {

    private final UserPasswordMapper userPasswordMapper;

    public UserPassword save(UserPassword userPassword) {
        UserPasswordDto userPasswordDto = UserPasswordDto.fromDomain(userPassword);
        userPasswordMapper.insert(userPasswordDto);
        return userPasswordDto.toDomain();
    }
}
