package com.epam.finaltask.restcontroller;

import com.epam.finaltask.auth.UserAdapter;
import com.epam.finaltask.auth.bruteforce.BruteForceAuthProvider;
import com.epam.finaltask.auth.bruteforce.BruteForceDefenceMechanism;
import com.epam.finaltask.config.ApplicationConfig;
import com.epam.finaltask.config.SecurityConfig;
import com.epam.finaltask.dto.Tokens;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.request.UserLoginRequest;
import com.epam.finaltask.exception.exceptions.RegistrationException;
import com.epam.finaltask.exception.exceptions.VoucherException;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.RefreshTokenService;
import com.epam.finaltask.service.UserService;

import com.epam.finaltask.token.JWTService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationRestController.class)
//importing configuration and BruteForceAuthenticationProvider to defend from Brute Force
@Import({SecurityConfig.class, ApplicationConfig.class, BruteForceAuthProvider.class})
@Log4j2
public class AuthenticationRestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BruteForceDefenceMechanism bruteForceDefenceMechanism;
    @MockBean
    private JWTService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private RefreshTokenService refreshTokenService;
    @MockBean
    private UserService userService;
    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Qualifier("jwtCookieName")
    @Autowired
    private String jwtCookie;

    @Test
    @DisplayName("Test POST /register endpoint")
    public void testRegisterEndpoint() throws Exception {
        var expect = UserDTO.builder()
                .username("username")
                .password("Some9assword!")
                .role("USER")
                .phoneNumber("0933900033")
                .balance(2D)
                .build();

        when(userService.register(any(UserDTO.class))).thenReturn(expect);

        var request = post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(expect))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expect)));

        verify(userService, times(1)).register(any(UserDTO.class));
    }

    @Test
    @DisplayName("Test POST /register endpoint, invalid password")
    public void testRegisterEndpoint_InvalidPassword() throws Exception {
        var requestBody = UserDTO.builder()
                .username("username")
                .password("123")
                .role("USER")
                .phoneNumber("0933900033")
                .balance(2D)
                .build();
        var expect = Map.of("exception", "Password must contain at least one digit, one lowercase letter, one uppercase letter," +
                        " one special character(!@#&()–[{}]:;',?/*~$^+=<>), and be between 8 and 20 characters");

        var request = post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().json(mapper.writeValueAsString(expect)));
    }

    @Test
    @DisplayName("Test POST /register endpoint, wrong role")
    public void testRegisterEndpoint_WrongRole() throws Exception {
        var requestBody = UserDTO.builder()
                .username("username")
                .password("Some9assword!")
                .role("bus")
                .phoneNumber("0933900033")
                .balance(2D)
                .build();

        when(userService.register(any(UserDTO.class))).thenThrow(new VoucherException("Wrong Role provided"));

        var request = post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exception\":\"Wrong Role provided\"}"));

        verify(userService, times(1)).register(any(UserDTO.class));
    }

    @Test
    @DisplayName("Test POST /register endpoint, user already exists")
    public void testRegisterEndpoint_UserAlreadyExists() throws Exception {
        var requestBody = UserDTO.builder()
                .username("username")
                .password("Some9assword!")
                .role("ADMIN")
                .phoneNumber("0933900033")
                .balance(2D)
                .build();

        when(userService.register(any(UserDTO.class))).thenThrow(new RegistrationException("Such a user already exists!"));

        var request = post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exception\":\"Such a user already exists!\"}"));

        verify(userService, times(1)).register(any(UserDTO.class));
    }

    @Test
    @DisplayName("Test POST /login endpoint")
    public void testLoginEndpoint() throws Exception {
        String username = "username";
        String password = "Some9assword!";
        var requestBody = UserLoginRequest.builder()
                .username(username)
                .password(password)
                .build();
        var userAdapter = new UserAdapter(User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .build());
        var expect = Tokens.builder()
                .jwtToken("exampleJWT")
                .refreshToken("exampleRefresh")
                .build();

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userAdapter);
        when(authenticationService.generateToken(username)).thenReturn(expect);

        var request = post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expect)));

        verify(bruteForceDefenceMechanism, times(1)).isBlocked(username);
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(bruteForceDefenceMechanism, times(1)).loginSucceeded(username);

        verify(authenticationService, times(1)).generateToken(username);
    }

    @Test
    @DisplayName("Test POST /login endpoint, user blocked")
    public void testLoginEndpoint_UserBlocked() throws Exception {
        String username = "username";
        var requestBody = UserLoginRequest.builder()
                .username(username)
                .password("Some9assword!")
                .build();

        when(bruteForceDefenceMechanism.isBlocked(username)).thenReturn(true);

        var request = post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exception\":\"You have been temporarily locked due to too many failed login attempts.\"}"));

        verify(bruteForceDefenceMechanism, times(1)).isBlocked(username);
    }

    @Test
    @DisplayName("Test POST /login endpoint, wrong password")
    public void testLoginEndpoint_WrongPassword() throws Exception {
        String username = "username";
        var requestBody = UserLoginRequest.builder()
                .username(username)
                .password("Some9assword!")
                .build();
        var userAdapter = new UserAdapter(User.builder()
                .username(username)
                .password(passwordEncoder.encode("123"))
                .build());

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userAdapter);

        var request = post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exception\":\"Invalid username or password.\"}"));

        verify(bruteForceDefenceMechanism, times(1)).isBlocked(username);
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(bruteForceDefenceMechanism, times(1)).loginFailed(username);
    }

    @Test
    @DisplayName("Test POST /logout endpoint, using header authorization")
    public void testLogoutEndpoint_HeaderAuthorization() throws Exception {
        String username = "username";
        String jwtToken = "jwtToken";
        var requestBody = Tokens.builder()
                .jwtToken(jwtToken)
                .refreshToken(UUID.randomUUID().toString())
                .build();
        var userDetails = new UserAdapter(User.builder()
                .username(username)
                .password("123")
                .role(Role.ADMIN)
                .build());

        when(jwtService.extractUsername(jwtToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.validateToken(jwtToken, userDetails)).thenReturn(true);
        when(refreshTokenService.isPresent(username)).thenReturn(true);

        var request = post("/api/auth/logout")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successfully"));
        //verify filter calls
        verify(jwtService, times(1)).extractUsername(jwtToken);
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtService, times(1)).validateToken(jwtToken, userDetails);
        verify(authenticationService, times(1)).authenticate(userDetails);
        verify(refreshTokenService, times(1)).isPresent(username);
        //verify controller calls
        verify(authenticationService, times(1)).deleteRefreshToken(any(Tokens.class));
        verify(authenticationService, times(1)).authenticateAnonymous();
    }

    @Test
    @DisplayName("Test POST /logout endpoint, using cookies")
    public void testLogoutEndpoint_Cookies() throws Exception {
        String username = "username";
        String jwtToken = "jwtToken";
        var requestBody = Tokens.builder()
                .jwtToken(jwtToken)
                .refreshToken(UUID.randomUUID().toString())
                .build();
        var userDetails = new UserAdapter(User.builder()
                .username(username)
                .password("123")
                .role(Role.ADMIN)
                .build());

        when(jwtService.extractUsername(jwtToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.validateToken(jwtToken, userDetails)).thenReturn(true);
        when(refreshTokenService.isPresent(username)).thenReturn(true);

        var request = post("/api/auth/logout")
                .cookie(new Cookie(jwtCookie, jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successfully"));
        //verify filter calls
        verify(jwtService, times(1)).extractUsername(jwtToken);
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtService, times(1)).validateToken(jwtToken, userDetails);
        verify(authenticationService, times(1)).authenticate(userDetails);
        verify(refreshTokenService, times(1)).isPresent(username);
        //verify controller calls
        verify(authenticationService, times(1)).deleteRefreshToken(any(Tokens.class));
        verify(authenticationService, times(1)).authenticateAnonymous();
    }
}
