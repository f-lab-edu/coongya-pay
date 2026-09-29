package com.flab.coongyapay.user.repository;

import com.flab.coongyapay.user.assembler.UserTransferPinAssembler;
import com.flab.coongyapay.user.domain.UserTransferPin;
import com.flab.coongyapay.user.mapper.UserTransferPinMapper;
import com.flab.coongyapay.user.mapper.dto.UserTransferPinDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserTransferPinRepository {

    private final UserTransferPinMapper userTransferPinMapper;
    private final UserTransferPinAssembler userTransferPinAssembler;

    public UserTransferPin save(UserTransferPin userTransferPin) {
        UserTransferPinDto userTransferPinDto = userTransferPinAssembler.toDto(userTransferPin);
        userTransferPinMapper.insert(userTransferPinDto);
        return userTransferPinAssembler.toDomain(userTransferPinDto);
    }

    public Optional<UserTransferPin> findByUserIdForUpdate(Long userId) {
        return userTransferPinMapper.findByUserIdForUpdate(userId)
                .map(userTransferPinAssembler::toDomain);
    }

    public int update(UserTransferPin userTransferPin) {
        UserTransferPinDto dto = userTransferPinAssembler.toDto(userTransferPin);
        return userTransferPinMapper.update(dto);
    }
}
