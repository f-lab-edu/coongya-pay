package com.flab.coongyapay.user.controller;

import com.flab.coongyapay.user.controller.dto.SignupRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserController {

    @PostMapping("/api/v1/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Void signup(@Valid @RequestBody SignupRequest request) {
        return null;
    }
}
