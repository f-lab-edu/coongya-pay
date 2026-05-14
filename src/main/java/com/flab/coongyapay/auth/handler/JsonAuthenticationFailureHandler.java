package com.flab.coongyapay.auth.handler;

import com.flab.coongyapay.common.exception.ErrorCode;
import com.flab.coongyapay.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    public JsonAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        ErrorCode errorCode = resolveErrorCode(exception);
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(errorCode));
    }

    private ErrorCode resolveErrorCode(AuthenticationException exception) {
        //1. Filter에서 던진 검증 실패 예외
        if (exception instanceof AuthenticationServiceException) {
            return ErrorCode.of(exception.getMessage())
                    .orElse(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        //2. Provider에서 던진 잠금 예외 (단, 인증 로직 순서를 조정하여 비밀번호 일치 여부부터 검사해서 이메일, 비밀번호 일치한 사용자에게만 계정 잠금 여부가 노출되도록 한다.)
        else if (exception instanceof LockedException) {
            return ErrorCode.ACCOUNT_LOCKED;
        }

        //3. 그 외
        return ErrorCode.LOGIN_FAILED;
    }
}
