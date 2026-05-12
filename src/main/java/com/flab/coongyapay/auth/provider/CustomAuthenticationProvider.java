package com.flab.coongyapay.auth.provider;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {


    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        //TODO 구현
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        //TODO 구현
        return false;
    }
}

