package com.flab.coongyapay.idempotency.mapper;

import com.flab.coongyapay.idempotency.mapper.dto.IdempotencyRecordDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface IdempotencyMapper {

    void insert(@Param("record") IdempotencyRecordDto record, @Param("leaseSeconds") long leaseSeconds, @Param("ttlSeconds") long ttlSeconds);

    Optional<IdempotencyRecordDto> findByPk(@Param("userId") Long userId, @Param("endpoint") String endpoint, @Param("idempotencyKey") String idempotencyKey);

    int reclaim(@Param("userId") Long userId, @Param("endpoint") String endpoint, @Param("idempotencyKey") String idempotencyKey, @Param("leaseSeconds") long leaseSeconds);

    int complete(@Param("userId") Long userId, @Param("endpoint") String endpoint, @Param("idempotencyKey") String idempotencyKey, @Param("responseHttpStatus") int responseHttpStatus, @Param("responseBody") String responseBody);

    int delete(@Param("userId") Long userId, @Param("endpoint") String endpoint, @Param("idempotencyKey") String idempotencyKey);
}
