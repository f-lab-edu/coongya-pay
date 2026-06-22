package com.flab.coongyapay.user.repository;

import com.flab.coongyapay.user.assembler.UserPasswordAssembler;
import com.flab.coongyapay.user.domain.UserPassword;
import com.flab.coongyapay.user.mapper.UserPasswordMapper;
import com.flab.coongyapay.user.mapper.dto.UserPasswordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserPasswordRepository {

    private final UserPasswordMapper userPasswordMapper;
    private final UserPasswordAssembler userPasswordAssembler;

    public UserPassword save(UserPassword userPassword) {
        UserPasswordDto userPasswordDto = userPasswordAssembler.toDto(userPassword);
        userPasswordMapper.insert(userPasswordDto);
        return userPasswordAssembler.toDomain(userPasswordDto);
    }

    public Optional<UserPassword> findByUserId(Long userId) {
        return userPasswordMapper.findByUserId(userId).map(userPasswordAssembler::toDomain);
    }

    public void incrementFailedLoginCount(Long userId, int lockThreshold, Duration lockDuration) {
        userPasswordMapper.incrementFailedLoginCount(userId, lockThreshold, lockDuration.toMinutes());
    }

    public void resetFailedLoginCount(Long userId) {
        userPasswordMapper.resetFailedLoginCount(userId);
    }
}
