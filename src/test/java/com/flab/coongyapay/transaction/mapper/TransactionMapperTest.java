package com.flab.coongyapay.transaction.mapper;

import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.enums.TransactionType;
import com.flab.coongyapay.transaction.mapper.dto.TransactionDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;

@MybatisTest
class TransactionMapperTest {

    @Autowired
    private TransactionMapper transactionMapper;

    @Test
    void insert_성공하면_id_자동채번() {
        TransactionDto dto = new TransactionDto(null, 1L, TransactionType.CHARGE.toString(), null, BigDecimal.ONE, TransactionStatus.CREATED.toString(), "김쿵야", null, null, null);
        transactionMapper.insert(dto);
        Assertions.assertThat(dto.getId()).isNotNull();
    }

    @Test
    void findById_아이디_없으면_Optional_empty() {
        TransactionDto dto = new TransactionDto(null, 1L, TransactionType.CHARGE.toString(), null, BigDecimal.ONE, TransactionStatus.CREATED.toString(), "김쿵야", null, null, null);
        transactionMapper.insert(dto);
        Optional<TransactionDto> optional = transactionMapper.findById(dto.getId()+1);
        Assertions.assertThat(optional).isEmpty();
    }

    @Test
    void findById_아이디_있으면_row_반환() {
        TransactionDto dto = new TransactionDto(null, 1L, TransactionType.CHARGE.toString(), null, BigDecimal.ONE, TransactionStatus.CREATED.toString(), "김쿵야", null, null, null);
        transactionMapper.insert(dto);
        Optional<TransactionDto> optional = transactionMapper.findById(dto.getId());
        Assertions.assertThat(optional).isPresent();
        Assertions.assertThat(optional.get().getWalletId()).isEqualTo(dto.getWalletId());
        Assertions.assertThat(optional.get().getTransactionType()).isEqualTo(dto.getTransactionType());
        Assertions.assertThat(optional.get().getAmount()).isEqualByComparingTo(dto.getAmount());
        Assertions.assertThat(optional.get().getStatus()).isEqualTo(dto.getStatus());
        Assertions.assertThat(optional.get().getRemark()).isEqualTo(dto.getRemark());
        Assertions.assertThat(optional.get().getCreatedAt()).isNotNull();
    }
}