package com.flab.coongyapay.user.mapper.dto;

import com.flab.coongyapay.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String name;

    public static UserDto fromDomain(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }

    public User toDomain() {
        return User.from(this.id, this.email, this.name);
    }
}
