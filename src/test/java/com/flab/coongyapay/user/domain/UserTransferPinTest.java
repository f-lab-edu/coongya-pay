package com.flab.coongyapay.user.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;


class UserTransferPinTest {

    private static final String transferPinHash = "hash";

    @Test
    void increaseFailedTransferPinCount시_카운트_1_증가() {
        UserTransferPin original = UserTransferPin.from(1L, 1L, transferPinHash, 0, null);

        UserTransferPin incremented = original.incrementFailedTransferPinCount();

        Assertions.assertThat(incremented.getFailedTransferPinCount()).isEqualTo(1);
    }

    @Test
    void 카운트_5회_도달시_잠김() {
        UserTransferPin original = UserTransferPin.from(1L, 1L, transferPinHash, 4, null);

        UserTransferPin incremented = original.incrementFailedTransferPinCount();

        Assertions.assertThat(incremented.getFailedTransferPinCount()).isEqualTo(5);
        Assertions.assertThat(incremented.isLockedAt(LocalDateTime.now())).isTrue();
    }

    @Test
    void 카운트_4회까지는_잠금_없음() {
        UserTransferPin original = UserTransferPin.from(1L, 1L, transferPinHash, 3, null);

        UserTransferPin incremented = original.incrementFailedTransferPinCount();

        Assertions.assertThat(incremented.getFailedTransferPinCount()).isEqualTo(4);
        Assertions.assertThat(incremented.isLockedAt(LocalDateTime.now())).isFalse();
    }

    @Test
    void resetFailedTransferPinCount시_카운트_및_잠금시각_리셋() {
        UserTransferPin original = UserTransferPin.from(1L, 1L, transferPinHash, 5, UserTransferPin.PERMANENT_LOCK);

        UserTransferPin reset = original.resetFailedTransferPinCount();

        Assertions.assertThat(reset.getFailedTransferPinCount()).isZero();
        Assertions.assertThat(reset.getLockedUntil()).isNull();
        Assertions.assertThat(reset.isLockedAt(LocalDateTime.now())).isFalse();
    }
}