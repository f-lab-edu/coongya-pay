package com.flab.coongyapay.idempotency.mapper;

import com.flab.coongyapay.idempotency.mapper.dto.IdempotencyRecordDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;


@MybatisTest
class IdempotencyMapperTest {

    private static final String ENDPOINT = "POST /api/v1/charges";
    private static final long LEASE_SECONDS = 120;
    private static final long EXPIRED_LEASE_SECONDS = -120;
    private static final long TTL_SECONDS = 86_400;

    @Autowired
    private IdempotencyMapper idempotencyMapper;

    @Test
    void 같은_PK로_두번_insert시_DuplicateKeyException() {
        idempotencyMapper.insert(newDto("key"), LEASE_SECONDS, TTL_SECONDS);
        Assertions.assertThatThrownBy(() -> {
            idempotencyMapper.insert(newDto("key"), LEASE_SECONDS, TTL_SECONDS);
        }).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void lease_만료된_PROCESSING만_reclaim_성공() {
        idempotencyMapper.insert(newDto("key"), EXPIRED_LEASE_SECONDS, TTL_SECONDS);

        int first = idempotencyMapper.reclaim(1L, ENDPOINT, "key", LEASE_SECONDS);
        Assertions.assertThat(first).isEqualTo(1);

        int second = idempotencyMapper.reclaim(1L, ENDPOINT, "key", LEASE_SECONDS);
        Assertions.assertThat(second).isEqualTo(0);
    }

    @Test
    void reclaim_후_lease_token_1_증가() {
        IdempotencyRecordDto dto = newDto("key");
        idempotencyMapper.insert(dto, EXPIRED_LEASE_SECONDS, TTL_SECONDS);

        int reclaimed = idempotencyMapper.reclaim(dto.getUserId(), dto.getEndpoint(), dto.getIdempotencyKey(), LEASE_SECONDS);
        Assertions.assertThat(reclaimed).isEqualTo(1);
        IdempotencyRecordDto recordDto = idempotencyMapper.findByPk(dto.getUserId(), dto.getEndpoint(), dto.getIdempotencyKey())
                .orElseThrow();
        Assertions.assertThat(recordDto.getLeaseToken()).isEqualTo(dto.getLeaseToken() + 1);
    }

    @Test
    void complete시_COMPLETED() {
        IdempotencyRecordDto dto = newDto("key");
        idempotencyMapper.insert(dto, LEASE_SECONDS, TTL_SECONDS);
        int affected = idempotencyMapper.complete(1L, ENDPOINT, "key", 202, "body", dto.getLeaseToken());
        Optional<IdempotencyRecordDto> optional = idempotencyMapper.findByPk(1L, ENDPOINT, "key");
        Assertions.assertThat(affected).isEqualTo(1);
        Assertions.assertThat(optional).isPresent();
        Assertions.assertThat(optional.get().getStatus()).isEqualTo("COMPLETED");
        Assertions.assertThat(optional.get().getResponseHttpStatus()).isEqualTo(202);
        Assertions.assertThat(optional.get().getResponseBody()).isEqualTo("body");
    }

    @Test
    void 옛날_토큰_complete_안_됨() {
        IdempotencyRecordDto dto = newDto("key");
        idempotencyMapper.insert(dto, EXPIRED_LEASE_SECONDS, TTL_SECONDS);
        int reclaimed = idempotencyMapper.reclaim(dto.getUserId(), dto.getEndpoint(), dto.getIdempotencyKey(), LEASE_SECONDS);

        Assertions.assertThat(reclaimed).isEqualTo(1);

        int completed = idempotencyMapper.complete(dto.getUserId(), dto.getEndpoint(), dto.getIdempotencyKey(), 200, "body", dto.getLeaseToken());

        Assertions.assertThat(completed).isEqualTo(0);

        IdempotencyRecordDto recordDto = idempotencyMapper.findByPk(dto.getUserId(), dto.getEndpoint(), dto.getIdempotencyKey()).orElseThrow();
        Assertions.assertThat(recordDto.getStatus()).isEqualTo("PROCESSING");
    }

    @Test
    void 새_토큰_complete_됨() {
        IdempotencyRecordDto dto = newDto("key");
        idempotencyMapper.insert(dto, LEASE_SECONDS, TTL_SECONDS);

        int completed = idempotencyMapper.complete(dto.getUserId(), dto.getEndpoint(), dto.getIdempotencyKey(), 200, "body", dto.getLeaseToken());

        Assertions.assertThat(completed).isEqualTo(1);

        IdempotencyRecordDto recordDto = idempotencyMapper.findByPk(dto.getUserId(), dto.getEndpoint(), dto.getIdempotencyKey()).orElseThrow();
        Assertions.assertThat(recordDto.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void delete_토큰() {
        IdempotencyRecordDto dto = newDto("key");
        idempotencyMapper.insert(dto, LEASE_SECONDS, TTL_SECONDS);

        int deleted = idempotencyMapper.delete(dto.getUserId(), dto.getEndpoint(), dto.getIdempotencyKey(), dto.getLeaseToken());

        Assertions.assertThat(deleted).isEqualTo(1);
        Assertions.assertThat(idempotencyMapper.findByPk(dto.getUserId(), dto.getEndpoint(), dto.getIdempotencyKey())).isEmpty();
    }

    @Test
    void 옛날_토큰_delete_안_됨() {
        IdempotencyRecordDto dto = newDto("key");
        idempotencyMapper.insert(dto, EXPIRED_LEASE_SECONDS, TTL_SECONDS);
        int reclaimed = idempotencyMapper.reclaim(dto.getUserId(), dto.getEndpoint(), dto.getIdempotencyKey(), LEASE_SECONDS);

        Assertions.assertThat(reclaimed).isEqualTo(1);

        int deleted = idempotencyMapper.delete(dto.getUserId(), dto.getEndpoint(), dto.getIdempotencyKey(), dto.getLeaseToken());

        Assertions.assertThat(deleted).isEqualTo(0);
        Assertions.assertThat(idempotencyMapper.findByPk(dto.getUserId(), dto.getEndpoint(), dto.getIdempotencyKey())).isPresent();
    }

    private IdempotencyRecordDto newDto(String key) {
        return new IdempotencyRecordDto(1L, ENDPOINT, key, "hash", "PROCESSING", null, null, 0L, null, null);
    }
}