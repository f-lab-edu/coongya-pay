package com.flab.coongyapay.user.assembler;

import com.flab.coongyapay.user.domain.UserPassword;
import com.flab.coongyapay.user.mapper.dto.UserPasswordDto;
import org.springframework.stereotype.Component;

@Component
public class UserPasswordAssembler {

    public UserPasswordDto toDto(UserPassword domain) {
        return new UserPasswordDto(
                domain.getId(),
                domain.getUserId(),
                domain.getPasswordHash(),
                domain.getFailedLoginCount(),
                domain.getLockedUntil()
        );
    }

    public UserPassword toDomain(UserPasswordDto dto) {
        return UserPassword.from(
                dto.getId(),
                dto.getUserId(),
                dto.getPassword(),
                dto.getFailedLoginCount(),
                dto.getLockedUntil()
        );
    }
}
