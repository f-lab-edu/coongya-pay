package com.flab.coongyapay.user.mapper;

import com.flab.coongyapay.user.mapper.dto.UserTransferPinDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserTransferPinMapper {

    void insert(UserTransferPinDto userTransferPinDto);
}
