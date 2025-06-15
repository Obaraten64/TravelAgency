package com.epam.finaltask.service;

import com.epam.finaltask.exception.exceptions.UserSearchException;
import com.epam.finaltask.model.RefreshToken;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.RefreshTokenRepository;
import com.epam.finaltask.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {
  @Mock
  private RefreshTokenRepository refreshTokenRepository;
  @Mock
  private UserRepository userRepository;
  private final long expirationLong = 10L;

  private RefreshTokenService refreshTokenService;

  @BeforeEach
  void setUp() {
    refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository, expirationLong);
  }

  @Test
  @DisplayName("Create refresh token, successfully")
  void testCreateRefreshToken() {
    // Given
    UUID id = UUID.randomUUID();
    String username = "admin";
    Date expirationDate = new Date(System.currentTimeMillis() + expirationLong);
    User user = User.builder()
            .id(UUID.randomUUID())
            .username(username)
            .password("123")
            .role(Role.USER)
            .build();
    RefreshToken expect = RefreshToken.builder()
            .id(id)
            .expiryDate(expirationDate)
            .user(user)
            .build();
    System.out.println(expect);
    // When
    when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(expect);
    // Then
    RefreshToken result = refreshTokenService.createRefreshToken(username);

    assertThat(result).isEqualTo(expect);

    verify(userRepository, times(1)).findUserByUsername(username);
    verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("Create refresh token, user not found")
  void testCreateRefreshToken_UserNotFound() {
    // Given
    String username = "admin";
    // When
    when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());
    // Then
    assertThatThrownBy(() -> refreshTokenService.createRefreshToken(username))
            .isInstanceOf(UserSearchException.class)
            .hasMessage("User not found!");

    verify(userRepository, times(1)).findUserByUsername(username);
  }

  @Test
  @DisplayName("Delete refresh token")
  void testDeleteRefreshToken() {
    // Given
    UUID token = UUID.randomUUID();
    // Then
    refreshTokenService.removeRefreshToken(token.toString());

    verify(refreshTokenRepository, times(1)).deleteById(token);
  }

  @Test
  @DisplayName("Delete refresh token, invalid token")
  void testDeleteRefreshToken_InvalidToken() {
    // Then
    assertThatThrownBy(() -> refreshTokenService.removeRefreshToken(null))
            .isInstanceOf(UserSearchException.class)
            .hasMessage("Invalid Refresh Token");
  }

  @Test
  @DisplayName("Is token expired")
  void testIsTokenExpired() {
    // Given
    UUID token = UUID.randomUUID();
    //When
    when(refreshTokenRepository.findById(token)).thenReturn(Optional.empty());
    // Then
    boolean result = refreshTokenService.isExpired(token.toString());
    assertThat(result).isTrue();

    verify(refreshTokenRepository, times(1)).findById(token);
  }

  @Test
  @DisplayName("Is token present")
  void testIsTokenPresent() {
    // Given
    String username = "admin";
    User user = User.builder()
            .username(username)
            .build();
    //When
    when(userRepository.findUserByUsername(username)).thenReturn(Optional.ofNullable(user));
    when(refreshTokenRepository.existsByUser(user)).thenReturn(false);
    // Then
    boolean result = refreshTokenService.isPresent(username);
    assertThat(result).isFalse();

    verify(userRepository, times(1)).findUserByUsername(username);
    verify(refreshTokenRepository, times(1)).existsByUser(user);
  }

  @Test
  @DisplayName("Get username from token")
  void testGetUsernameFromToken() {
    // Given
    UUID token = UUID.randomUUID();
    //When
    when(refreshTokenRepository.findById(token)).thenReturn(Optional.empty());
    // Then
    String result = refreshTokenService.getUsername(token.toString());
    assertThat(result).isNull();

    verify(refreshTokenRepository, times(1)).findById(token);
  }
}
