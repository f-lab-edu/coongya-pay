package com.flab.coongyapay.charge.controller.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChargeRequest {

    @NotNull(message = "FIELD_REQUIRED")
    private Long bankAccountId;

    @NotNull(message = "FIELD_REQUIRED")
    @DecimalMin(value = "1", message = "INVALID_CHARGE_AMOUNT")
    @DecimalMax(value = "2000000", message = "INVALID_CHARGE_AMOUNT")
    private BigDecimal amount;

    @Size(min = 1, max = 7, message = "INVALID_REMARK_LENGTH")
    private String remark;

    @NotBlank(message = "FIELD_REQUIRED")
    @Pattern(regexp = "^\\d{6}$", message = "INVALID_TRANSFER_PIN_FORMAT")
    private String transferPin;
}
