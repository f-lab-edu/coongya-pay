package com.flab.coongyapay.user.mapper.dto;

import com.flab.coongyapay.user.domain.UserPassword;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordDto {
    private Long id;
    private Long userId;
    private String password;
    private int failedLoginCount;
    private LocalDateTime lockedUntil;

    public static UserPasswordDto fromDomain(UserPassword userPassword) {
        return new UserPasswordDto(userPassword.getId(), userPassword.getUserId(), userPassword.getPasswordHash(), userPassword.getFailedLoginCount(), userPassword.getLockedUntil());
    }

    public UserPassword toDomain() {
        return UserPassword.from(this.id, this.userId, this.password, this.failedLoginCount, this.lockedUntil);
    }
}
