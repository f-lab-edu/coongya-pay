package com.flab.coongyapay.user.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTransferPinDto {
    private Long id;
    private Long userId;
    private String transferPin;
    private int failedTransferPinCount;
    private LocalDateTime lockedUntil;
}
