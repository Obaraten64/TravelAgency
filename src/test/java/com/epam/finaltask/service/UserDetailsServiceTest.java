package com.epam.finaltask.service;

import com.epam.finaltask.auth.UserAdapter;
import com.epam.finaltask.auth.UserDetailsServiceImpl;
import com.epam.finaltask.dto.request.UserLoginRequest;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.token.JWTService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceTest {
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserDetailsServiceImpl userDetailsService;

  @Test
  @DisplayName("Load by username, successfully")
  void testLoad() {
    // Given
    UUID id = UUID.randomUUID();
    String username = "admin";
    String password = "123";
    User user = User.builder()
            .id(id)
            .username(username)
            .password(password)
            .role(Role.USER)
            .build();
    UserAdapter userDetails = new UserAdapter(user);
    // When
    when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
    // Then
    UserDetails result = userDetailsService.loadUserByUsername(username);

    assertThat(result)
            .isInstanceOf(UserAdapter.class)
            .hasFieldOrPropertyWithValue("user", user);

    verify(userRepository, times(1)).findUserByUsername(username);
  }

  @Test
  @DisplayName("Load by user name, user not found")
  void testLoad_UserNotFound() {
    // Given
    String username = "admin";
    // When
    when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());
    // Then
    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found");

    verify(userRepository, times(1)).findUserByUsername(username);
  }
}
