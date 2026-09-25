package com.flab.coongyapay.user.mapper;

import com.flab.coongyapay.user.mapper.dto.UserTransferPinDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserTransferPinMapper {

    void insert(UserTransferPinDto userTransferPinDto);

    Optional<UserTransferPinDto> findByUserIdForUpdate(Long userId);

    int update(UserTransferPinDto userTransferPinDto);
}
