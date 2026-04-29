package com.flab.coongyapay.user.mapper.dto;

import com.flab.coongyapay.user.domain.UserTransferPin;
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

    public static UserTransferPinDto fromDomain(UserTransferPin userTransferPin) {
        return new UserTransferPinDto(userTransferPin.getId(), userTransferPin.getUserId(), userTransferPin.getTransferPinHash(), userTransferPin.getFailedTransferPinCount(), userTransferPin.getLockedUntil());
    }

    public UserTransferPin toDomain() {
        return UserTransferPin.from(this.id, this.userId, this.transferPin, this.failedTransferPinCount, this.lockedUntil);
    }
}
