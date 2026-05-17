package com.flab.coongyapay.config;

import com.flab.coongyapay.auth.filter.JsonUsernamePasswordAuthenticationFilter;
import com.flab.coongyapay.auth.handler.JsonAuthenticationFailureHandler;
import com.flab.coongyapay.auth.handler.JsonAuthenticationSuccessHandler;
import com.flab.coongyapay.auth.provider.CustomAuthenticationProvider;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler;
    private final JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(CustomAuthenticationProvider customAuthenticationProvider) {
        return new ProviderManager(customAuthenticationProvider);
    }

    @Bean
    public SessionRegistry sessionRegistry(@Autowired(required = false) FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        // Test 환경 fallback
        if (sessionRepository == null) {
            return new SessionRegistryImpl();
        }
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        JsonUsernamePasswordAuthenticationFilter filter = new JsonUsernamePasswordAuthenticationFilter(objectMapper, validator);
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(jsonAuthenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(jsonAuthenticationFailureHandler);
        return filter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter, SessionRegistry sessionRegistry) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/signup", "/api/v1/login").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(fixation -> fixation.changeSessionId())
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(sessionRegistry)
                )
                .addFilterAt(jsonUsernamePasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
