package com.flab.coongyapay.transaction.mapper;

import com.flab.coongyapay.transaction.mapper.dto.RetryStateDto;
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

    Optional<TransactionDto> findById(@Param("id") Long id);

    BigDecimal sumInFlightChargeByWalletId(@Param("walletId") Long walletId);

    Optional<TransactionDto> findChargeByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    Optional<TransactionDto> findByParentTransactionId(@Param("parentTransactionId") Long parentTransactionId);

    // ===== 비동기 워커: 선점/펜싱/재시도 =====

    List<Long> selectClaimableIds(@Param("transactionType") String transactionType,
                                  @Param("statuses") List<String> statuses,
                                  @Param("limit") int limit);

    void acquireLease(@Param("id") Long id, @Param("leaseSeconds") int leaseSeconds);

    long findLeaseToken(@Param("id") Long id);

    int updateStatus(@Param("id") Long id,
                     @Param("expectedStatus") String expectedStatus,
                     @Param("newStatus") String newStatus,
                     @Param("failureReason") String failureReason,
                     @Param("completedAt") LocalDateTime completedAt);

    int updateStatusFenced(@Param("id") Long id,
                           @Param("expectedStatus") String expectedStatus,
                           @Param("newStatus") String newStatus,
                           @Param("failureReason") String failureReason,
                           @Param("completedAt") LocalDateTime completedAt,
                           @Param("leaseToken") long leaseToken);

    int scheduleRetry(@Param("id") Long id,
                      @Param("backoffSeconds") int backoffSeconds,
                      @Param("leaseToken") long leaseToken);

    int incrementRequeryCount(@Param("id") Long id, @Param("leaseToken") long leaseToken);

    RetryStateDto findRetryState(@Param("id") Long id);

    void assignExternalIdempotencyKey(@Param("id") Long id,
                                      @Param("externalIdempotencyKey") String externalIdempotencyKey);
}
