package com.flab.coongyapay.account.controller.dto;

import com.flab.coongyapay.account.domain.BankAccount;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class AccountResponse {

    private static final String MASKING_CHARACTER = "*";

    private final Long id;
    private final String bankCode;
    private final String accountNumber;
    private final String accountHolderName;
    private final LocalDateTime registeredAt;

    public static AccountResponse from(BankAccount bankAccount) {
        return new AccountResponse(
                bankAccount.getId(),
                bankAccount.getBankCode(),
                maskAccountNumber(bankAccount.getAccountNumber()),
                bankAccount.getAccountHolderName(),
                bankAccount.getRegisteredAt()
        );
    }

    private static String maskAccountNumber(String accountNumber) {
        // 앞/뒤 4자리만 노출
        int length = accountNumber.length();
        if (length <= 8) {
            return accountNumber.replaceAll("\\d", MASKING_CHARACTER);
        }

        String prefix = accountNumber.substring(0, 4);
        String masked = accountNumber.substring(4, length - 4).replaceAll("\\d", MASKING_CHARACTER);
        String suffix = accountNumber.substring(length - 4);

        return prefix + masked + suffix;
    }
}
