package com.flab.coongyapay.user.repository;

import com.flab.coongyapay.user.assembler.UserAssembler;
import com.flab.coongyapay.user.domain.User;
import com.flab.coongyapay.user.mapper.UserMapper;
import com.flab.coongyapay.user.mapper.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final UserMapper userMapper;
    private final UserAssembler userAssembler;

    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    public Optional<User> findByEmailForUpdate(String email) {
        return userMapper.findByEmailForUpdate(email).map(userAssembler::toDomain);
    }

    public User save(User user) {
        UserDto userDto = userAssembler.toDto(user);
        userMapper.insert(userDto);
        return userAssembler.toDomain(userDto);
    }
}
