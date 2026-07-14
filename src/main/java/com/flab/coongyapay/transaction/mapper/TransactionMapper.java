package com.flab.coongyapay.transaction.mapper;

import com.flab.coongyapay.transaction.mapper.dto.TransactionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface TransactionMapper {

    void insert(TransactionDto dto);

    Optional<TransactionDto> findById(@Param("id") Long id);
}
