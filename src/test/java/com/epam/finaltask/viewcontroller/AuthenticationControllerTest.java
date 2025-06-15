package com.epam.finaltask.viewcontroller;

import com.epam.finaltask.auth.bruteforce.BruteForceAuthProvider;
import com.epam.finaltask.config.ApplicationConfig;
import com.epam.finaltask.config.SecurityConfig;
import com.epam.finaltask.dto.Tokens;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.request.UserLoginRequest;
import com.epam.finaltask.dto.request.UserRegistrationRequest;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.restcontroller.AuthenticationRestController;
import com.epam.finaltask.restcontroller.viewscontroller.AuthenticationController;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.RefreshTokenService;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.token.JWTService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
//importing configuration
@Import({SecurityConfig.class, ApplicationConfig.class})
@Log4j2
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BruteForceAuthProvider bruteForceAuthProvider;
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
    @MockBean
    private AuthenticationRestController authenticationRestController;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Qualifier("jwtCookieName")
    @Autowired
    private String jwtCookie;
    @Qualifier("jwtRefreshCookieName")
    @Autowired
    private String jwtRefreshCookie;

    @Test
    @DisplayName("Test GET /sign-in endpoint")
    public void testSingInEndpoint() throws Exception {
        var request = get("/auth/sign-in");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("/auth/sign-in"));
    }

    @Test
    @DisplayName("Test GET /sign-in endpoint, with error message")
    public void testSingInEndpoint_WithError() throws Exception {
        String error = "error";
        var request = get("/auth/sign-in")
                .param("error", error);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", error))
                .andExpect(view().name("/auth/sign-in"));
    }

    @Test
    @DisplayName("Test GET /sign-up endpoint")
    public void testSingUpEndpoint() throws Exception {
        var request = get("/auth/sign-up");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", new UserRegistrationRequest()))
                .andExpect(model().attribute("roles", Role.values()))
                .andExpect(view().name("/auth/sign-up"));
    }

    @Test
    @DisplayName("Test POST /register endpoint")
    public void testRegisterEndpoint() throws Exception {
        var requestBody = UserRegistrationRequest.builder()
                .username("username")
                .password("Some9assword!")
                .role("bus")
                .phoneNumber("0933900033")
                .balance(2D)
                .build();
        var expect = UserDTO.builder()
                .username("username")
                .password("Some9assword!")
                .role("bus")
                .phoneNumber("0933900033")
                .balance(2D)
                .build();

        when(authenticationRestController.registerUser(requestBody)).thenReturn(expect);

        var request = post("/auth/register")
                .flashAttr("user", requestBody);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("/auth/sign-in"));

        verify(authenticationRestController, times(1)).registerUser(requestBody);
    }

    @Test
    @DisplayName("Test POST /register endpoint, invalid password")
    public void testRegisterEndpoint_InvalidPassword() throws Exception {
        var requestBody = UserRegistrationRequest.builder()
                .username("username")
                .password("123")
                .role("USER")
                .phoneNumber("0933900033")
                .balance(2D)
                .build();
        var expect = "Password must contain at least one digit, one lowercase letter, one uppercase letter," +
                " one special character(!@#&()–[{}]:;',?/*~$^+=<>), and be between 8 and 20 characters";

        var request = post("/auth/register")
                .flashAttr("user", requestBody);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(model().attribute("exception", expect))
                .andExpect(view().name("/error"));
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
        var expect = Tokens.builder()
                .jwtToken("exampleJWT")
                .refreshToken("exampleRefresh")
                .build();

        when(authenticationRestController.login(requestBody)).thenReturn(expect);

        var request = post("/auth/login")
                .flashAttr("user", requestBody);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("/index"));

        verify(authenticationRestController, times(1)).login(requestBody);

        verify(authenticationService, times(2))
                .updateCookie(any(HttpServletResponse.class), any(Cookie.class), eq(Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("Test POST /logout endpoint")
    public void testLogoutEndpoint() throws Exception {
        String jwtToken = "jwtToken";
        var requestBody = Tokens.builder()
                .jwtToken(jwtToken)
                .refreshToken(UUID.randomUUID().toString())
                .build();

        var request = post("/auth/logout")
                .cookie(new Cookie(jwtCookie, requestBody.getJwtToken()),
                        new Cookie(jwtRefreshCookie, requestBody.getRefreshToken()));
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("/index"));

        verify(authenticationRestController, times(1)).logout(requestBody);
        verify(authenticationService, times(2))
                .updateCookie(any(HttpServletResponse.class), any(Cookie.class), eq(0));
    }

    @Test
    @DisplayName("Test POST /logout endpoint, no cookies")
    public void testLogoutEndpoint_NoCookies() throws Exception {
        var request = post("/auth/logout");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attribute("exception", "You are not logged in"))
                .andExpect(view().name("/error"));
    }
}
