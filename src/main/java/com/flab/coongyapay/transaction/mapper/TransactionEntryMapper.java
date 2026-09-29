package com.flab.coongyapay.transaction.mapper;

import com.flab.coongyapay.transaction.mapper.dto.TransactionEntryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface TransactionEntryMapper {

    void insert(TransactionEntryDto dto);

    Optional<TransactionEntryDto> findByTransactionIdAndEntryType(@Param("transactionId") Long transactionId,
                                                                 @Param("entryType") String entryType);
}
