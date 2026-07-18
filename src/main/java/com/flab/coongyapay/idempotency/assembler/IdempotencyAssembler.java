package com.flab.coongyapay.idempotency.assembler;

import com.flab.coongyapay.idempotency.domain.IdempotencyRecord;
import com.flab.coongyapay.idempotency.enums.IdempotencyStatus;
import com.flab.coongyapay.idempotency.mapper.dto.IdempotencyRecordDto;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyAssembler {

    public IdempotencyRecordDto toDto(IdempotencyRecord domain) {
        return new IdempotencyRecordDto(domain.getUserId(), domain.getEndpoint(), domain.getIdempotencyKey(), domain.getRequestHash(), domain.getStatus().name(), domain.getResponseHttpStatus(), domain.getResponseBody(), null, null);
    }

    public IdempotencyRecord toDomain(IdempotencyRecordDto dto) {
        return IdempotencyRecord.from(dto.getUserId(), dto.getEndpoint(), dto.getIdempotencyKey(), dto.getRequestHash(), dto.getStatus() == null ? null : IdempotencyStatus.valueOf(dto.getStatus()), dto.getResponseHttpStatus(), dto.getResponseBody());
    }
}
