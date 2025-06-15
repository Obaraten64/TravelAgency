package com.epam.finaltask.service;

import com.epam.finaltask.dto.Tokens;
import com.epam.finaltask.token.JWTService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthenticationService {
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;

    public Tokens generateToken(String username) {
        //check if user already authorized
        if (refreshTokenService.isPresent(username)) {
            log.info("This user: {} already authorized", username);
            throw new BadCredentialsException("This user already authorized");
        }
        //generate tokens for user
        log.info("Generating tokens for: {} ", username);
        String token = jwtService.generateToken(username);
        String refreshToken = refreshTokenService.createRefreshToken(username)
                .getId().toString();

        return Tokens.builder()
                .jwtToken(token)
                .refreshToken(refreshToken)
                .build();
    }

    public void deleteRefreshToken(Tokens tokens) {
        //show JWT token
        log.info("Logout request from user with token: {}", tokens.getJwtToken());
        //delete refreshToken from db
        refreshTokenService.removeRefreshToken(tokens.getRefreshToken());
    }

    public void authenticate(UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public void authenticateAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "anonymous", "anonymous", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    }

    public void updateCookie(HttpServletResponse response, Cookie cookie, Integer cookieAge) {
        cookie.setMaxAge(cookieAge);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}
