package com.flab.coongyapay.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "FIELD_REQUIRED")
    private String email;

    @NotBlank(message = "FIELD_REQUIRED")
    private String password;
}
