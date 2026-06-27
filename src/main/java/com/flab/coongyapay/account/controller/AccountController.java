package com.flab.coongyapay.account.controller;

import com.flab.coongyapay.account.controller.dto.AccountRegisterRequest;
import com.flab.coongyapay.account.controller.dto.AccountResponse;
import com.flab.coongyapay.account.service.AccountService;
import com.flab.coongyapay.auth.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/api/v1/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountRegisterRequest request) {
        accountService.register(userDetails.getUser().getId(), request);
    }

    @GetMapping("/api/v1/accounts")
    public List<AccountResponse> getAccounts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return accountService.getAccounts(userDetails.getUser().getId());
    }

    @DeleteMapping("/api/v1/accounts/{id}")
    public void delete(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        accountService.deleteAccount(id, userDetails.getUser().getId());
    }
}
