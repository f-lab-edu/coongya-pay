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
    void complete시_COMPLETED() {
        idempotencyMapper.insert(newDto("key"), LEASE_SECONDS, TTL_SECONDS);
        int affected = idempotencyMapper.complete(1L, ENDPOINT, "key", 202, "body");
        Optional<IdempotencyRecordDto> optional = idempotencyMapper.findByPk(1L, ENDPOINT, "key");
        Assertions.assertThat(affected).isSameAs(1);
        Assertions.assertThat(optional).isPresent();
        Assertions.assertThat(optional.get().getStatus()).isEqualTo("COMPLETED");
        Assertions.assertThat(optional.get().getResponseHttpStatus()).isEqualTo(202);
        Assertions.assertThat(optional.get().getResponseBody()).isEqualTo("body");
    }

    private IdempotencyRecordDto newDto(String key) {
        return new IdempotencyRecordDto(1L, ENDPOINT, key, "hash", "PROCESSING", null, null, null, null);
    }
}