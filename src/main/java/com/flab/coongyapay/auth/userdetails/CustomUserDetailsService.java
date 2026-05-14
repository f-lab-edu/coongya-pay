package com.flab.coongyapay.auth.userdetails;

import com.flab.coongyapay.user.domain.User;
import com.flab.coongyapay.user.domain.UserPassword;
import com.flab.coongyapay.user.repository.UserPasswordRepository;
import com.flab.coongyapay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserPasswordRepository userPasswordRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        UserPassword userPassword = userPasswordRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("UserPassword Not Found"));

        return new CustomUserDetails(user, userPassword);
    }
}
