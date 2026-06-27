package com.flab.coongyapay.account.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountRegisterRequest {

    @NotBlank(message = "FIELD_REQUIRED")
    private String bankCode;

    @NotBlank(message = "FIELD_REQUIRED")
    @Pattern(regexp = "^\\d{7,15}$", message = "INVALID_ACCOUNT_NUMBER_FORMAT")
    private String accountNumber;

    @NotBlank(message = "FIELD_REQUIRED")
    private String accountHolderName;
}
