package com.flab.coongyapay.idempotency.repository;

import com.flab.coongyapay.idempotency.assembler.IdempotencyAssembler;
import com.flab.coongyapay.idempotency.domain.IdempotencyRecord;
import com.flab.coongyapay.idempotency.mapper.IdempotencyMapper;
import com.flab.coongyapay.idempotency.mapper.dto.IdempotencyRecordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IdempotencyRepository {

    public static final int LEASE_SECONDS = 120;
    public static final int TTL_SECONDS = 86_400;

    private final IdempotencyMapper idempotencyMapper;
    private final IdempotencyAssembler idempotencyAssembler;

    public boolean tryInsertProcessing(IdempotencyRecord idempotencyRecord) {
        try {
            IdempotencyRecordDto dto = idempotencyAssembler.toDto(idempotencyRecord);
            idempotencyMapper.insert(dto, LEASE_SECONDS, TTL_SECONDS);
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    public Optional<IdempotencyRecord> findByPk(Long userId, String endpoint, String idempotencyKey) {
        return idempotencyMapper.findByPk(userId, endpoint, idempotencyKey).map(idempotencyAssembler::toDomain);
    }

    public boolean reclaim(Long userId, String endpoint, String idempotencyKey) {
        int reclaimed = idempotencyMapper.reclaim(userId, endpoint, idempotencyKey, LEASE_SECONDS);
        return reclaimed == 1;
    }

    public boolean complete(Long userId, String endpoint, String idempotencyKey, int responseHttpStatus, String responseBody) {
        int completed = idempotencyMapper.complete(userId, endpoint, idempotencyKey, responseHttpStatus, responseBody);
        return completed == 1;
    }

    public void release(Long userId, String endpoint, String idempotencyKey) {
        idempotencyMapper.delete(userId, endpoint, idempotencyKey);
    }
}
