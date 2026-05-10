package com.flab.coongyapay.user.assembler;

import com.flab.coongyapay.user.domain.UserLoginHistory;
import com.flab.coongyapay.user.mapper.dto.UserLoginHistoryDto;
import org.springframework.stereotype.Component;

@Component
public class UserLoginHistoryAssembler {

    public UserLoginHistoryDto toDto(UserLoginHistory domain) {
        return new UserLoginHistoryDto(
                domain.getId(),
                domain.getUserId(),
                domain.isSuccess(),
                domain.getFailureReason(),
                domain.getLoginAt(),
                domain.getIpAddress()
        );
    }

    public UserLoginHistory toDomain(UserLoginHistoryDto dto) {
        return UserLoginHistory.from(
                dto.getId(),
                dto.getUserId(),
                dto.isSuccess(),
                dto.getFailureReason(),
                dto.getLoginAt(),
                dto.getIpAddress(
                ));
    }
}
