package com.flab.coongyapay.user.repository;

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

    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    public Optional<User> findByEmailForUpdate(String email) {
        return userMapper.findByEmailForUpdate(email).map(UserDto::toDomain);
    }

    public User save(User user) {
        UserDto userDto = UserDto.fromDomain(user);
        userMapper.insert(userDto);
        return userDto.toDomain();
    }
}
