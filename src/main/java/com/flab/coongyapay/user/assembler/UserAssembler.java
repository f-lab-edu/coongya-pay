package com.flab.coongyapay.user.assembler;

import com.flab.coongyapay.user.domain.User;
import com.flab.coongyapay.user.mapper.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserAssembler {

    public UserDto toDto(User domain) {
        return new UserDto(
                domain.getId(),
                domain.getEmail(),
                domain.getName()
        );
    }

    public User toDomain(UserDto dto) {
        return User.from(
                dto.getId(),
                dto.getEmail(),
                dto.getName()
        );
    }
}
