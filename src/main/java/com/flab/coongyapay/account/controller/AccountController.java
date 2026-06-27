package com.flab.coongyapay.account.controller;

import com.flab.coongyapay.account.controller.dto.AccountRegisterRequest;
import com.flab.coongyapay.account.service.AccountService;
import com.flab.coongyapay.auth.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/api/v1/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountRegisterRequest request) {
        accountService.register(userDetails.getUser().getId(), request);
    }
}
