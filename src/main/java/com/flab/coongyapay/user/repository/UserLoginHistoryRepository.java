package com.flab.coongyapay.user.repository;

import com.flab.coongyapay.user.assembler.UserLoginHistoryAssembler;
import com.flab.coongyapay.user.domain.UserLoginHistory;
import com.flab.coongyapay.user.mapper.UserLoginHistoryMapper;
import com.flab.coongyapay.user.mapper.dto.UserLoginHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserLoginHistoryRepository {

    private final UserLoginHistoryMapper userLoginHistoryMapper;
    private final UserLoginHistoryAssembler userLoginHistoryAssembler;

    public UserLoginHistory save(UserLoginHistory userLoginHistory) {
        UserLoginHistoryDto dto = userLoginHistoryAssembler.toDto(userLoginHistory);
        userLoginHistoryMapper.insert(dto);
        return userLoginHistoryAssembler.toDomain(dto);
    }
}
