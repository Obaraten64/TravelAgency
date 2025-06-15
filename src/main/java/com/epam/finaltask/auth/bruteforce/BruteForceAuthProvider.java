package com.epam.finaltask.auth.bruteforce;

import lombok.AllArgsConstructor;

import lombok.extern.log4j.Log4j2;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Log4j2
public class BruteForceAuthProvider implements AuthenticationProvider {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final BruteForceDefenceMechanism bruteForceDefenceMechanism;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        log.info("Attempting to authenticate user: {}", username);
        String password = (String) authentication.getCredentials();

        if (bruteForceDefenceMechanism.isBlocked(username)) {
            log.info("User {} is blocked", username);
            throw new BadCredentialsException("You have been temporarily locked due to too many failed login attempts.");
        }
        UserDetails user = userDetailsService.loadUserByUsername(username);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            bruteForceDefenceMechanism.loginFailed(username);
            log.info("User {} failed login attempt", username);
            throw new BadCredentialsException("Invalid username or password.");
        }

        bruteForceDefenceMechanism.loginSucceeded(username);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        return authToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
