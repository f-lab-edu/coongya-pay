package com.flab.coongyapay.idempotency.service;

import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.idempotency.domain.IdempotencyRecord;
import com.flab.coongyapay.idempotency.enums.IdempotencyStatus;
import com.flab.coongyapay.idempotency.repository.IdempotencyRepository;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    private static final String ENDPOINT = "POST /api/v1/charges";
    private static final String KEY = "key";
    private static final String HASH = "hash1";
    public static final String RESPONSE_BODY = "{\"chargeId\":1}";

    @InjectMocks
    private IdempotencyService idempotencyService;
    @Mock
    private IdempotencyRepository idempotencyRepository;

    @Test
    void 신규키면_newRequest_반환() {
        when(idempotencyRepository.tryInsertProcessing(any())).thenReturn(true);

        ClaimResult result = idempotencyService.claim(1L, ENDPOINT, KEY, HASH);

        Assertions.assertThat(result.isReplayed()).isFalse();
        verify(idempotencyRepository, never()).findByPk(any(), any(), any());
        verify(idempotencyRepository, never()).reclaim(any(), any(), any());
    }

    @Test
    void 해시_다르면_IDEMPOTENCY_KEY_CONFLICT_던짐() {
        when(idempotencyRepository.tryInsertProcessing(any())).thenReturn(false);
        when(idempotencyRepository.findByPk(1L, ENDPOINT, KEY))
                .thenReturn(Optional.of(record("hash2", IdempotencyStatus.PROCESSING, null, null)));

        Assertions.assertThatThrownBy(() -> idempotencyService.claim(1L, ENDPOINT, KEY, HASH))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);

        verify(idempotencyRepository, never()).reclaim(any(), any(), any());
    }

    @Test
    void 완료된_키면_캐시응답반환() {
        when(idempotencyRepository.tryInsertProcessing(any())).thenReturn(false);
        when(idempotencyRepository.findByPk(1L, ENDPOINT, KEY)).thenReturn(Optional.of(record(HASH, IdempotencyStatus.COMPLETED, HttpStatus.SC_ACCEPTED, RESPONSE_BODY)));

        ClaimResult result = idempotencyService.claim(1L, ENDPOINT, KEY, HASH);

        Assertions.assertThat(result.isReplayed()).isTrue();
        Assertions.assertThat(result.getResponseHttpStatus()).isEqualTo(HttpStatus.SC_ACCEPTED);
        Assertions.assertThat(result.getCachedResponseBody()).isEqualTo(RESPONSE_BODY);
    }

    @Test
    void 처리중_키_reclaim_성공시_newRequest_반환() {
        when(idempotencyRepository.tryInsertProcessing(any())).thenReturn(false);
        when(idempotencyRepository.findByPk(1L, ENDPOINT, KEY)).thenReturn(Optional.of(record(HASH, IdempotencyStatus.PROCESSING, null, null)));
        when(idempotencyRepository.reclaim(1L, ENDPOINT, KEY)).thenReturn(true);

        ClaimResult result = idempotencyService.claim(1L, ENDPOINT, KEY, HASH);

        Assertions.assertThat(result.isReplayed()).isFalse();
        Assertions.assertThat(result.getResponseHttpStatus()).isNull();
        Assertions.assertThat(result.getCachedResponseBody()).isNull();
    }

    @Test
    void 처리중_키_reclaim_실패시_IDEMPOTENCY_KEY_PROCESSING_던짐() {
        when(idempotencyRepository.tryInsertProcessing(any())).thenReturn(false);
        when(idempotencyRepository.findByPk(1L, ENDPOINT, KEY)).thenReturn(Optional.of(record(HASH, IdempotencyStatus.PROCESSING, null, null)));
        when(idempotencyRepository.reclaim(1L, ENDPOINT, KEY)).thenReturn(false);

        Assertions.assertThatThrownBy(() -> {
            idempotencyService.claim(1L, ENDPOINT, KEY, HASH);
        })
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.IDEMPOTENCY_KEY_PROCESSING);
    }

    private IdempotencyRecord record(String hash, IdempotencyStatus status, Integer responseHttpStatus, String responseBody) {
        return IdempotencyRecord.from(1L, ENDPOINT, KEY, hash, status, responseHttpStatus, responseBody);
    }

}