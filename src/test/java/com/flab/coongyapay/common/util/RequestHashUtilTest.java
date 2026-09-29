package com.flab.coongyapay.common.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;


class RequestHashUtilTest {

    @Test
    void sha256HexTest_abc() {
        String sha256Hex = RequestHashUtil.sha256Hex("abc");

        Assertions.assertThat(sha256Hex).isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void sha256HexTest_빈_문자열() {
        String sha256Hex = RequestHashUtil.sha256Hex("");

        Assertions.assertThat(sha256Hex).isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }
}