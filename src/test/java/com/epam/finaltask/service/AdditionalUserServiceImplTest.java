package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.exceptions.RegistrationException;
import com.epam.finaltask.exception.exceptions.UserSearchException;
import com.epam.finaltask.exception.exceptions.VoucherException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdditionalUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Register user, successfully")
    public void testRegisterUserSuccessfully() {
        //Given
        String username = "genius";
        String password = "Genius1";
        UserDTO expected = UserDTO.builder()
                .username(username)
                .password(password)
                .role("USER")
                .balance(35.35)
                .build();
        //When
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(password);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(expected);
        //Then
        assertThat(userService.register(expected)).isEqualTo(expected);

        verify(userRepository, times(1)).findUserByUsername(username);
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toUserDTO(any(User.class));
    }

    @Test
    @DisplayName("Register user, already exist")
    public void testRegisterUser_AlreadyExist() {
        //Given
        String username = "genius";
        String password = "Genius1";
        UserDTO expected = UserDTO.builder()
                .username(username)
                .password(password)
                .role("USER")
                .balance(35.35)
                .build();
        //When
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(new User()));
        //Then
        assertThatThrownBy(() -> userService.register(expected))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("Such a user already exists!");

        verify(userRepository, times(1)).findUserByUsername(username);
    }

    @Test
    @DisplayName("Register user, wrong role")
    public void testRegisterUser_WrongRole() {
        //Given
        String username = "genius";
        String password = "Genius1";
        UserDTO expected = UserDTO.builder()
                .username(username)
                .password(password)
                .balance(35.35)
                .build();
        //When
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(password);
        //Then
        assertThatThrownBy(() -> userService.register(expected))
                .isInstanceOf(VoucherException.class)
                .hasMessage("Wrong Role provided");

        verify(userRepository, times(1)).findUserByUsername(username);
        verify(passwordEncoder, times(1)).encode(password);
    }

    @Test
    @DisplayName("Update user, successfully")
    public void testUpdateUser() {
        String username = "user1";
        User user = User.builder()
                .username(username)
                .role(Role.ADMIN)
                .build();
        UserDTO toUpdate = UserDTO.builder()
                .username(username)
                .role("USER")
                .build();
        User updatedUser = User.builder()
                .username(username)
                .role(Role.USER)
                .build();

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(toUpdate);

        UserDTO result = userService.updateUser(username, toUpdate);

        assertThat(result).isEqualTo(toUpdate);

        verify(userRepository, times(2)).findUserByUsername(username);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toUserDTO(any(User.class));
    }

    @Test
    @DisplayName("Update user, not admin trying to update")
    public void testUpdateUser_NotAdmin() {
        String username = "user1";
        User notAdmin = User.builder()
                .username(username)
                .role(Role.MANAGER)
                .build();
        UserDTO toUpdate = UserDTO.builder()
                .username("user2")
                .build();

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(notAdmin));

        assertThatThrownBy(() -> userService.updateUser(username, toUpdate))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("You are not allowed to update this user!");

        verify(userRepository, times(1)).findUserByUsername(username);
    }
}
