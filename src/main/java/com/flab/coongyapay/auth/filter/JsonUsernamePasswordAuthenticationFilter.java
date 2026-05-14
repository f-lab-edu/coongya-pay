package com.flab.coongyapay.auth.filter;

import com.flab.coongyapay.auth.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;

public class JsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public JsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper, Validator validator) {
        super(PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.POST, "/api/v1/login"));
        this.objectMapper = objectMapper;
        // Filter에서 파라미터 검증을 위해 Jakarta Validator를 직접 호출
        this.validator = validator;
        // IP address 추출을 위해 WebAuthenticationDetailsSource 등록
        setAuthenticationDetailsSource(new WebAuthenticationDetailsSource());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //1. Content-Type 검사
        if (request.getContentType() == null
                || !request.getContentType().equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)) {
            throw new AuthenticationServiceException("Unsupported content type: " + request.getContentType());
        }

        //2. JSON 역직렬화
        LoginRequest loginRequest;
        try {
            loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Error parsing login request", e);
        }

        //3. Bean Validation
        Set<ConstraintViolation<LoginRequest>> violationSet = validator.validate(loginRequest);
        if (!violationSet.isEmpty()) {
            String code = violationSet.iterator().next().getMessage();
            throw new AuthenticationServiceException(code);
        }

        //4. 인증 Token 생성, AuthenticationManager 위임
        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.getEmail(), loginRequest.getPassword());
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));

        return getAuthenticationManager().authenticate(authRequest);
    }
}
