package com.flab.coongyapay.user.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "FIELD_REQUIRED")
    @Email(message = "INVALID_EMAIL_FORMAT")
    private String email;

    @NotBlank(message = "FIELD_REQUIRED")
    @Size(min=8, max=32, message = "INVALID_PASSWORD_LENGTH")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$", message = "INVALID_PASSWORD_FORMAT")
    private String password;

    @NotBlank(message = "FIELD_REQUIRED")
    @Size(min=1, max=30, message = "INVALID_NAME_LENGTH")
    @Pattern(regexp = "^([가-힣]+|[A-Za-z ]+)$", message = "INVALID_NAME_FORMAT")
    private String name;

    @NotBlank(message = "FIELD_REQUIRED")
    @Pattern(regexp = "^\\d{6}$", message = "INVALID_TRANSFER_PIN_FORMAT")
    private String transferPin;
}
