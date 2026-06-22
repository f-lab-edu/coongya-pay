package com.flab.coongyapay.auth.provider;

import com.flab.coongyapay.auth.service.LoginService;
import com.flab.coongyapay.auth.userdetails.CustomUserDetails;
import com.flab.coongyapay.auth.userdetails.CustomUserDetailsService;
import com.flab.coongyapay.user.domain.UserPassword;
import com.flab.coongyapay.user.enums.LoginFailureReason;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final LoginService loginService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());
        String ipAddress = extractIpAddress(authentication);

        //1. 회원 조회
        CustomUserDetails customUserDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(email);
        Long userId = customUserDetails.getUser().getId();
        UserPassword userPassword = customUserDetails.getUserPassword();

        //2. 비밀번호 검증
        if (!passwordEncoder.matches(password, userPassword.getPasswordHash())) {
            loginService.loginFail(userId, ipAddress, LoginFailureReason.INVALID_PASSWORD);
            throw new BadCredentialsException("Bad credentials");
        }

        //3. 계정 잠금 체크
        if (userPassword.isLockedAt(LocalDateTime.now())) {
            loginService.loginFail(userId, ipAddress, LoginFailureReason.ACCOUNT_LOCKED);
            throw new LockedException("Account is locked");
        }

        //4. 인증 성공 처리, 인증 토큰 반환
        loginService.loginSuccess(userId, ipAddress);
        return UsernamePasswordAuthenticationToken.authenticated(
                customUserDetails, null, customUserDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private String extractIpAddress(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof WebAuthenticationDetails) {
            return ((WebAuthenticationDetails) details).getRemoteAddress();
        }
        return null;
    }
}

