package com.flab.coongyapay.user.assembler;

import com.flab.coongyapay.user.domain.UserTransferPin;
import com.flab.coongyapay.user.mapper.dto.UserTransferPinDto;
import org.springframework.stereotype.Component;

@Component
public class UserTransferPinAssembler {

    public UserTransferPinDto toDto(UserTransferPin domain) {
        return new UserTransferPinDto(
                domain.getId(),
                domain.getUserId(),
                domain.getTransferPinHash(),
                domain.getFailedTransferPinCount(),
                domain.getLockedUntil()
        );
    }

    public UserTransferPin toDomain(UserTransferPinDto dto) {
        return UserTransferPin.from(
                dto.getId(),
                dto.getUserId(),
                dto.getTransferPin(),
                dto.getFailedTransferPinCount(),
                dto.getLockedUntil()
        );
    }
}
