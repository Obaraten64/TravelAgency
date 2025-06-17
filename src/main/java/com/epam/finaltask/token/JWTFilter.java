package com.epam.finaltask.token;

import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.RefreshTokenService;

import io.jsonwebtoken.ExpiredJwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Log4j2
public class JWTFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    @Qualifier("jwtCookieName")
    private final String jwtCookie;
    @Qualifier("jwtRefreshCookieName")
    private final String jwtRefreshCookie;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        //try to set JWT from cookie, if not found
        Optional<String> jwtTokenFromHeader = Optional.ofNullable(request.getHeader("Authorization"));
        String authHeader = jwtTokenFromHeader.orElseGet(() -> {
            log.info("Authorization token is empty. Trying to retrieve it from cookies!");
            return getToken(request, jwtCookie);
        });
        //extract token itself and username from it
        if (authHeader == null) {
            log.info("Authorization token is empty. No authorization");
            filterChain.doFilter(request, response);
            return;
        }
        String token = !authHeader.startsWith("Bearer ") ?
                authHeader : authHeader.substring(7);
        log.info("Bearer token found: {}", token);
        String username;
        //check if token expired
        try {
            //get username from non expired token
            username = jwtService.extractUsername(token);
            //check whether user authenticated or not
            if (!refreshTokenService.isPresent(username)) {
                log.info("User tried to access application using random JWT token");
                filterChain.doFilter(request, response);
                return;
            }
        } catch (ExpiredJwtException exception) {
            //refresh token
            log.info("Authorization token expired: {}. Refreshing", token);
            token = refreshJWT(request, response);
            //get username form newly created token
            log.info("Refreshed JWT token: {}", token);
            username = Optional.ofNullable(token)
                    .map(jwtService::extractUsername)
                    .orElse(null);
        }
        //get user if username exists and not authenticated
        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        log.info("User trying to authorize: {}", username);
        //validate token
        if (jwtService.validateToken(token, userDetails)) {
            log.info("User's {} token validated", username);
            authenticationService.authenticate(userDetails);
        }

        filterChain.doFilter(request, response);
    }

    private String refreshJWT(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //get and validate refresh token
        String refreshToken = getToken(request, jwtRefreshCookie);
        if (refreshToken == null || refreshTokenService.isExpired(refreshToken)) {
            log.info("Refresh token expired");
            refreshTokenService.removeRefreshToken(refreshToken);
            authenticationService.updateCookie(response, new Cookie(jwtCookie, null), 0);
            authenticationService.updateCookie(response, new Cookie(jwtRefreshCookie, null), 0);
            response.sendRedirect("/auth/sign-in?error=Your tokens have expired." +
                    " So you are logged out. Please log in again.");
            return null;
        }
        //refresh jwt token
        log.info("JWT token refreshing");
        return jwtService.generateToken(refreshTokenService.getUsername(refreshToken));
    }

    private String getToken(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    String token = cookie.getValue();
                    log.info("Found {} in cookies: {}", cookieName, token);
                    return token;
                }
            }
        }
        return null;
    }
}
