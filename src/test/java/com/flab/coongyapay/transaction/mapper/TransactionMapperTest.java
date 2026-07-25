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
        TransactionDto dto = getDto();
        transactionMapper.insert(dto);
        Assertions.assertThat(dto.getId()).isNotNull();
    }

    @Test
    void findById_아이디_없으면_Optional_empty() {
        TransactionDto dto = getDto();
        transactionMapper.insert(dto);
        Optional<TransactionDto> optional = transactionMapper.findById(dto.getId()+1);
        Assertions.assertThat(optional).isEmpty();
    }

    @Test
    void findById_아이디_있으면_row_반환() {
        TransactionDto dto = getDto();
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

    @Test
    void sumInFlightChargeByWalletId_처리중_충전_금액_반환() {
        TransactionDto createdDto = getDto();
        createdDto.setAmount(new BigDecimal(10_000));
        transactionMapper.insert(createdDto);

        TransactionDto withdrawingDto = getDto();
        withdrawingDto.setAmount(new BigDecimal(20_000));
        withdrawingDto.setStatus(TransactionStatus.WITHDRAWING.toString());
        transactionMapper.insert(withdrawingDto);

        TransactionDto withdrawnDto = getDto();
        withdrawnDto.setAmount(new BigDecimal(30_000));
        withdrawnDto.setStatus(TransactionStatus.WITHDRAWN.toString());
        transactionMapper.insert(withdrawnDto);

        TransactionDto depositingDto = getDto();
        depositingDto.setAmount(new BigDecimal(40_000));
        depositingDto.setStatus(TransactionStatus.DEPOSITING.toString());
        transactionMapper.insert(depositingDto);

        TransactionDto unknownDto = getDto();
        unknownDto.setAmount(new BigDecimal(50_000));
        unknownDto.setStatus(TransactionStatus.UNKNOWN.toString());
        transactionMapper.insert(unknownDto);

        TransactionDto compensatingDto = getDto();
        compensatingDto.setAmount(new BigDecimal(60_000));
        compensatingDto.setStatus(TransactionStatus.COMPENSATING.toString());
        transactionMapper.insert(compensatingDto);

        TransactionDto completedDto = getDto();
        completedDto.setAmount(new BigDecimal(70_000));
        completedDto.setStatus(TransactionStatus.COMPLETED.toString());
        transactionMapper.insert(completedDto);

        BigDecimal sumInFlightChargeByWalletId = transactionMapper.sumInFlightChargeByWalletId(1L);

        Assertions.assertThat(sumInFlightChargeByWalletId).isEqualByComparingTo(BigDecimal.valueOf(210_000));
    }

    @Test
    void sumInFlightChargeByWalletId_행_없으면_0_반환() {
        TransactionDto dto = getDto();
        transactionMapper.insert(dto);
        BigDecimal sumInFlightChargeByWalletId = transactionMapper.sumInFlightChargeByWalletId(dto.getWalletId()+1);

        Assertions.assertThat(sumInFlightChargeByWalletId).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private static TransactionDto getDto() {
        return new TransactionDto(null, 1L, TransactionType.CHARGE.toString(), null, BigDecimal.ONE, TransactionStatus.CREATED.toString(), "김쿵야", null, null, null);
    }
}