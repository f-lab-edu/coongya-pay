package com.flab.coongyapay.user.repository;

import com.flab.coongyapay.user.domain.UserTransferPin;
import com.flab.coongyapay.user.mapper.UserTransferPinMapper;
import com.flab.coongyapay.user.mapper.dto.UserTransferPinDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserTransferPinRepository {

    private final UserTransferPinMapper userTransferPinMapper;

    public UserTransferPin save(UserTransferPin userTransferPin) {
        UserTransferPinDto userTransferPinDto = UserTransferPinDto.fromDomain(userTransferPin);
        userTransferPinMapper.insert(userTransferPinDto);
        return userTransferPinDto.toDomain();
    }
}
