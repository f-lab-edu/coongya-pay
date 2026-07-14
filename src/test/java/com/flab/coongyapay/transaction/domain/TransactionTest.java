package com.flab.coongyapay.transaction.domain;

import com.flab.coongyapay.transaction.enums.TransactionStatus;
import com.flab.coongyapay.transaction.enums.TransactionType;
import com.flab.coongyapay.common.exception.BusinessException;
import com.flab.coongyapay.common.exception.ErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class TransactionTest {

    @Test
    void createCharge_성공하면_CREATED_1원() {
        Transaction chargeTransaction = Transaction.createCharge(1L, BigDecimal.ONE, "김쿵야");
        Assertions.assertThat(chargeTransaction.getStatus()).isSameAs(TransactionStatus.CREATED);
        Assertions.assertThat(chargeTransaction.getTransactionType()).isSameAs(TransactionType.CHARGE);
        Assertions.assertThat(chargeTransaction.getRemark()).isEqualTo("김쿵야");
        Assertions.assertThat(chargeTransaction.getAmount()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void createCharge_성공하면_CREATED_2백만원() {
        Transaction chargeTransaction = Transaction.createCharge(1L, BigDecimal.valueOf(2_000_000), "김쿵야");
        Assertions.assertThat(chargeTransaction.getStatus()).isSameAs(TransactionStatus.CREATED);
        Assertions.assertThat(chargeTransaction.getTransactionType()).isSameAs(TransactionType.CHARGE);
        Assertions.assertThat(chargeTransaction.getRemark()).isEqualTo("김쿵야");
        Assertions.assertThat(chargeTransaction.getAmount()).isEqualTo(BigDecimal.valueOf(2_000_000));
    }

    @Test
    void createCharge_금액_1원_미만이면_INVALID_CHARGE_AMOUNT_던짐() {
        Assertions.assertThatThrownBy(() -> {
            Transaction.createCharge(1L, BigDecimal.ZERO, "김쿵야");
        }).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_CHARGE_AMOUNT);
    }

    @Test
    void createCharge_금액_2백만원_초과면_INVALID_CHARGE_AMOUNT_던짐() {
        Assertions.assertThatThrownBy(() -> {
            Transaction.createCharge(1L, BigDecimal.valueOf(2_000_001), "김쿵야");
        }).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_CHARGE_AMOUNT);
    }

    @Test
    void createCharge_remark_길이_7자_초과면_INVALID_REMARK_LENGTH_던짐() {
        Assertions.assertThatThrownBy(() -> {
            Transaction.createCharge(1L, BigDecimal.ONE, "일이삼사오육칠팔");
        }).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REMARK_LENGTH);
    }
}