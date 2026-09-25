package com.flab.coongyapay.account.mapper;

import com.flab.coongyapay.account.mapper.dto.BankAccountDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@MybatisTest
class BankAccountMapperTest {

    @Autowired
    private BankAccountMapper bankAccountMapper;

    @Test
    void insert시_id_자동채번() {
        BankAccountDto bankAccountDto = getDto();
        bankAccountMapper.insert(bankAccountDto);

        Assertions.assertThat(bankAccountDto.getId()).isNotNull();
    }

    @Test
    void countActiveByUserId_사용자별_계좌수_반환() {
        BankAccountDto bankAccountDto = getDto();
        bankAccountMapper.insert(bankAccountDto);

        int count = bankAccountMapper.countActiveByUserId(getDto().getUserId());

        Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    void existsActiveByUserIdAndAccount_사용자_계좌_존재여부_반환() {
        BankAccountDto bankAccountDto = getDto();
        bankAccountMapper.insert(bankAccountDto);

        boolean exists = bankAccountMapper.existsActiveByUserIdAndAccount(bankAccountDto.getUserId(), bankAccountDto.getBankCode(), bankAccountDto.getAccountNumber());

        Assertions.assertThat(exists).isTrue();
    }

    @Test
    void findActiveByUserId_사용자_활성계좌_반환() {
        BankAccountDto bankAccountDto = getDto();
        bankAccountMapper.insert(bankAccountDto);

        List<BankAccountDto> activeByUserId = bankAccountMapper.findActiveByUserId(getDto().getUserId());

        Assertions.assertThat(activeByUserId).hasSize(1);
        Assertions.assertThat(activeByUserId.get(0).getUserId()).isEqualTo(bankAccountDto.getUserId());
        Assertions.assertThat(activeByUserId.get(0).getBankCode()).isEqualTo(bankAccountDto.getBankCode());
        Assertions.assertThat(activeByUserId.get(0).getAccountNumber()).isEqualTo(bankAccountDto.getAccountNumber());
        Assertions.assertThat(activeByUserId.get(0).getAccountHolderName()).isEqualTo(bankAccountDto.getAccountHolderName());
    }

    @Test
    void findActiveByUserId_사용자_비활성계좌_반환_안_함() {
        BankAccountDto bankAccountDto = getDto();
        bankAccountMapper.insert(bankAccountDto);
        int deleted = bankAccountMapper.softDelete(bankAccountDto.getId(), bankAccountDto.getUserId());

        List<BankAccountDto> activeByUserId = bankAccountMapper.findActiveByUserId(getDto().getUserId());

        Assertions.assertThat(deleted).isEqualTo(1);
        Assertions.assertThat(activeByUserId).hasSize(0);
    }

    @Test
    void findActiveByIdAndUserId_사용자_소유_활성계좌_반환() {
        BankAccountDto bankAccountDto = getDto();
        bankAccountMapper.insert(bankAccountDto);

        Optional<BankAccountDto> optional = bankAccountMapper.findActiveByIdAndUserId(bankAccountDto.getId(), getDto().getUserId());

        Assertions.assertThat(optional.isPresent()).isTrue();
        Assertions.assertThat(optional.get().getUserId()).isEqualTo(bankAccountDto.getUserId());
        Assertions.assertThat(optional.get().getBankCode()).isEqualTo(bankAccountDto.getBankCode());
        Assertions.assertThat(optional.get().getAccountNumber()).isEqualTo(bankAccountDto.getAccountNumber());
        Assertions.assertThat(optional.get().getAccountHolderName()).isEqualTo(bankAccountDto.getAccountHolderName());
    }

    @Test
    void findActiveByIdAndUserId_다른_사용자_소유_계좌_반환_안_함() {
        BankAccountDto bankAccountDto = getDto();
        bankAccountMapper.insert(bankAccountDto);

        Optional<BankAccountDto> optional = bankAccountMapper.findActiveByIdAndUserId(bankAccountDto.getId(), 2L);

        Assertions.assertThat(optional.isPresent()).isFalse();
    }

    @Test
    void findActiveByIdAndUserId_본인_소유_비활성계좌_반환_안_함() {
        BankAccountDto bankAccountDto = getDto();
        bankAccountMapper.insert(bankAccountDto);
        int deleted = bankAccountMapper.softDelete(bankAccountDto.getId(), bankAccountDto.getUserId());

        Optional<BankAccountDto> optional = bankAccountMapper.findActiveByIdAndUserId(bankAccountDto.getId(), bankAccountDto.getUserId());

        Assertions.assertThat(deleted).isEqualTo(1);
        Assertions.assertThat(optional.isPresent()).isFalse();
    }

    @Test
    void softDelete_삭제_후_재등록_가능() {
        BankAccountDto bankAccountDto = getDto();
        bankAccountMapper.insert(bankAccountDto);
        int deleted = bankAccountMapper.softDelete(bankAccountDto.getId(), bankAccountDto.getUserId());

        bankAccountMapper.insert(bankAccountDto);
        Optional<BankAccountDto> optional = bankAccountMapper.findActiveByIdAndUserId(bankAccountDto.getId(), bankAccountDto.getUserId());

        Assertions.assertThat(deleted).isEqualTo(1);
        Assertions.assertThat(optional.isPresent()).isTrue();
        Assertions.assertThat(optional.get().getUserId()).isEqualTo(bankAccountDto.getUserId());
        Assertions.assertThat(optional.get().getBankCode()).isEqualTo(bankAccountDto.getBankCode());
        Assertions.assertThat(optional.get().getAccountNumber()).isEqualTo(bankAccountDto.getAccountNumber());
        Assertions.assertThat(optional.get().getAccountHolderName()).isEqualTo(bankAccountDto.getAccountHolderName());
    }

    private static BankAccountDto getDto() {
        BankAccountDto bankAccountDto = new BankAccountDto();
        bankAccountDto.setUserId(1L);
        bankAccountDto.setBankCode("bankCode");
        bankAccountDto.setAccountNumber("111122223333");
        bankAccountDto.setAccountHolderName("김쿵야");
        return bankAccountDto;
    }

}