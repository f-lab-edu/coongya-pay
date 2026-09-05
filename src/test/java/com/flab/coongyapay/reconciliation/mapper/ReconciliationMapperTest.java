package com.flab.coongyapay.reconciliation.mapper;

import com.flab.coongyapay.transaction.enums.TransactionEntryType;
import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.enums.TransactionType;
import com.flab.coongyapay.transaction.mapper.TransactionEntryMapper;
import com.flab.coongyapay.transaction.mapper.TransactionMapper;
import com.flab.coongyapay.transaction.mapper.dto.TransactionDto;
import com.flab.coongyapay.transaction.mapper.dto.TransactionEntryDto;
import com.flab.coongyapay.wallet.mapper.WalletMapper;
import com.flab.coongyapay.wallet.mapper.dto.WalletDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@MybatisTest
class ReconciliationMapperTest {

    @Autowired private ReconciliationMapper reconciliationMapper;
    @Autowired private TransactionMapper transactionMapper;
    @Autowired private TransactionEntryMapper transactionEntryMapper;
    @Autowired private WalletMapper walletMapper;

    @Test
    void expireIdleCreatedCharges_유휴_CREATED만_EXPIRED() {
        TransactionDto created = chargeDto(TransactionStatus.CREATED);
        transactionMapper.insert(created);
        TransactionDto withdrawing = chargeDto(TransactionStatus.CREATED);
        transactionMapper.insert(withdrawing);
        transactionMapper.updateStatus(withdrawing.getId(), TransactionStatus.CREATED.name(),
                TransactionStatus.WITHDRAWING.name(), null, null);

        int expired = reconciliationMapper.expireIdleCreatedCharges(0);

        Assertions.assertThat(expired).isGreaterThanOrEqualTo(1);
        Assertions.assertThat(transactionMapper.findById(created.getId()).orElseThrow().getStatus())
                .isEqualTo(TransactionStatus.EXPIRED.name());
        // WITHDRAWING은 만료되지 않음(출금 위험)
        Assertions.assertThat(transactionMapper.findById(withdrawing.getId()).orElseThrow().getStatus())
                .isEqualTo(TransactionStatus.WITHDRAWING.name());
    }

    @Test
    void countCompletedChargesWithoutSingleCredit_CREDIT없는_COMPLETED충전_탐지() {
        int before = reconciliationMapper.countCompletedChargesWithoutSingleCredit();

        TransactionDto completed = chargeDto(TransactionStatus.COMPLETED);
        transactionMapper.insert(completed);

        // CREDIT 원장 없음 → 위반 +1
        Assertions.assertThat(reconciliationMapper.countCompletedChargesWithoutSingleCredit() - before)
                .isEqualTo(1);

        // CREDIT 1개 추가 → 위반 해소
        transactionEntryMapper.insert(new TransactionEntryDto(null, completed.getId(), 1L,
                TransactionEntryType.CREDIT.name(), BigDecimal.ONE, BigDecimal.ONE, 1L, null));
        Assertions.assertThat(reconciliationMapper.countCompletedChargesWithoutSingleCredit() - before)
                .isEqualTo(0);
    }

    @Test
    void countBalanceMismatches_원장없이_잔액이_0아니면_불일치() {
        int before = reconciliationMapper.countBalanceMismatches();

        // 원장이 없는데 잔액 100 → 마지막 balance_after(=0)와 불일치
        walletMapper.insert(new WalletDto(null, uniqueUserId(), BigDecimal.valueOf(100), 0));

        Assertions.assertThat(reconciliationMapper.countBalanceMismatches() - before).isEqualTo(1);
    }

    private TransactionDto chargeDto(TransactionStatus status) {
        return new TransactionDto(null, 1L, 1L, TransactionType.CHARGE.name(), null,
                BigDecimal.ONE, status.name(), "쿵야", null, null, null);
    }

    private Long uniqueUserId() {
        return System.nanoTime() % 1_000_000_000L;
    }
}
