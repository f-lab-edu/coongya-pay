package com.flab.coongyapay.user.mapper.dto;

import com.flab.coongyapay.user.enums.LoginFailureReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginHistoryDto {
    private Long id;
    private Long userId;
    private boolean success;
    private LoginFailureReason failureReason;
    private LocalDateTime loginAt;
    private String ipAddress;
}
