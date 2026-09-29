package com.flab.coongyapay.transaction.mapper;

import com.flab.coongyapay.transaction.enums.TransactionEntryType;
import com.flab.coongyapay.transaction.mapper.dto.TransactionEntryDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.util.Optional;

@MybatisTest
class TransactionEntryMapperTest {

    @Autowired
    private TransactionEntryMapper transactionEntryMapper;

    @Test
    void insert_성공하면_id_자동채번() {
        TransactionEntryDto dto = getDto(1L, 1L);
        transactionEntryMapper.insert(dto);

        Assertions.assertThat(dto.getId()).isNotNull();
    }

    @Test
    void findByTransactionIdAndEntryType_존재하면_반환() {
        TransactionEntryDto dto = getDto(1L, 1L);
        transactionEntryMapper.insert(dto);

        Optional<TransactionEntryDto> found = transactionEntryMapper.findByTransactionIdAndEntryType(1L, TransactionEntryType.CREDIT.name());

        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getAmount()).isEqualByComparingTo(dto.getAmount());
        Assertions.assertThat(found.get().getBalanceAfter()).isEqualByComparingTo(dto.getBalanceAfter());
        Assertions.assertThat(found.get().getWalletSequence()).isEqualTo(dto.getWalletSequence());
    }

    @Test
    void findByTransactionIdAndEntryType_없으면_empty() {
        Optional<TransactionEntryDto> found = transactionEntryMapper.findByTransactionIdAndEntryType(999L, TransactionEntryType.CREDIT.name());

        Assertions.assertThat(found).isEmpty();
    }

    @Test
    void 동일_거래에_CREDIT_중복_기입되면_UNIQUE_위반() {
        transactionEntryMapper.insert(getDto(1L, 1L));

        // (transaction_id, entry_type) UNIQUE → 같은 거래의 두 번째 CREDIT은 차단 = 이중 입금 방지 백스톱
        Assertions.assertThatThrownBy(() -> transactionEntryMapper.insert(getDto(1L, 1L)))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 동일_지갑_순번_중복이면_UNIQUE_위반() {
        transactionEntryMapper.insert(getDto(1L, 1L));

        // (wallet_id, wallet_sequence) UNIQUE → 같은 지갑의 같은 순번은 차단 = 순번 무결성
        TransactionEntryDto sameSequence = getDto(2L, 1L); // 다른 거래, 같은 지갑/순번
        Assertions.assertThatThrownBy(() -> transactionEntryMapper.insert(sameSequence))
                .isInstanceOf(DuplicateKeyException.class);
    }

    private TransactionEntryDto getDto(long transactionId, long walletSequence) {
        return new TransactionEntryDto(
                null,
                transactionId,
                1L,
                TransactionEntryType.CREDIT.name(),
                BigDecimal.valueOf(10_000),
                BigDecimal.valueOf(10_000),
                walletSequence,
                null
        );
    }
}
