package com.flab.coongyapay.user.mapper;

import com.flab.coongyapay.user.mapper.dto.UserPasswordDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserPasswordMapper {

    void insert(UserPasswordDto userPasswordDto);

    Optional<UserPasswordDto> findByUserId(@Param("userId") Long userId);

    void resetExpiredLock(@Param("userId") Long userId);

    void bumpFailedLoginCount(@Param("userId") Long userId,
                              @Param("lockThreshold") int lockThreshold,
                              @Param("lockDuration") long lockDuration);

    default void incrementFailedLoginCount(Long userId, int lockThreshold, long lockDuration) {
        resetExpiredLock(userId);
        bumpFailedLoginCount(userId, lockThreshold, lockDuration);
    }

    void resetFailedLoginCount(@Param("userId") Long userId);
}
