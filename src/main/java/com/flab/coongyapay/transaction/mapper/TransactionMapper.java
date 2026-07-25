package com.flab.coongyapay.transaction.mapper;

import com.flab.coongyapay.transaction.mapper.dto.TransactionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface TransactionMapper {

    void insert(TransactionDto dto);

    List<Long> findProcessableChargeIds(@Param("limit") int limit);

    Optional<TransactionDto> findById(@Param("id") Long id);

    BigDecimal sumInFlightChargeByWalletId(@Param("walletId") Long walletId);

    Optional<TransactionDto> findChargeByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int updateStatus(@Param("id") Long id,
                     @Param("expectedStatus") String expectedStatus,
                     @Param("newStatus") String newStatus,
                     @Param("failureReason") String failureReason,
                     @Param("completedAt") LocalDateTime completedAt);
}
