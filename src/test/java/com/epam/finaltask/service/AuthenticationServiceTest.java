package com.epam.finaltask.service;

import com.epam.finaltask.dto.Tokens;
import com.epam.finaltask.model.RefreshToken;

import com.epam.finaltask.token.JWTService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.BadCredentialsException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
  @Mock
  private JWTService jwtService;
  @Mock
  private RefreshTokenService refreshTokenService;

  @InjectMocks
  private AuthenticationService authenticationService;

  @Test
  @DisplayName("Generate tokens, successfully")
  void testGenerateTokens() {
    String username = "admin";
    String jwtToken = "123";
    UUID refreshTokenId = UUID.randomUUID();
    RefreshToken refreshToken = RefreshToken.builder()
            .id(refreshTokenId)
            .build();
    Tokens expect = Tokens.builder()
            .jwtToken(jwtToken)
            .refreshToken(refreshTokenId.toString())
            .build();

    when(refreshTokenService.isPresent(username)).thenReturn(false);
    when(jwtService.generateToken(username)).thenReturn(jwtToken);
    when(refreshTokenService.createRefreshToken(username)).thenReturn(refreshToken);

    Tokens result = authenticationService.generateToken(username);

    assertThat(result).isEqualTo(expect);

    verify(refreshTokenService, times(1)).isPresent(username);
    verify(jwtService, times(1)).generateToken(username);
    verify(refreshTokenService, times(1)).createRefreshToken(username);
  }

  @Test
  @DisplayName("Generate tokens, already exists")
  void testGenerateTokens_AlreadyExists() {
    String username = "admin";

    when(refreshTokenService.isPresent(username)).thenReturn(true);

    assertThatThrownBy(() -> authenticationService.generateToken(username))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("This user already authorized");

    verify(refreshTokenService, times(1)).isPresent(username);
  }

  @Test
  @DisplayName("Delete tokens")
  void testDeleteTokens() {
    String refreshToken = "123";
    Tokens tokens = Tokens.builder()
            .jwtToken("jwt")
            .refreshToken(refreshToken)
            .build();

    authenticationService.deleteRefreshToken(tokens);

    verify(refreshTokenService, times(1)).removeRefreshToken(refreshToken);
  }
}
