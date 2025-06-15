package com.epam.finaltask.auth.bruteforce;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Log4j2
public class AuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        log.error("Authentication failed for the reason: {}", errorMessage);
        getRedirectStrategy().sendRedirect(request, response, "/api/auth/sign-in?error=" + errorMessage);
    }
}
