package com.epam.finaltask.restcontroller;

import com.epam.finaltask.auth.UserAdapter;
import com.epam.finaltask.auth.bruteforce.BruteForceAuthProvider;
import com.epam.finaltask.config.ApplicationConfig;
import com.epam.finaltask.config.SecurityConfig;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.request.UserUpdateRequest;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.RefreshTokenService;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.token.JWTService;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRestController.class)
//importing configuration
@Import({SecurityConfig.class, ApplicationConfig.class})
@Log4j2
public class JWTFilterTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

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

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Qualifier("jwtCookieName")
    @Autowired
    private String jwtCookie;
    @Qualifier("jwtRefreshCookieName")
    @Autowired
    private String jwtRefreshCookie;
    //perform authorization before every request
    String jwtToken = "jwtExample";
    String username = "username";
    UserAdapter userDetails = new UserAdapter(User.builder()
            .username(username)
            .password("123")
            .role(Role.ADMIN)
            .build());
    @BeforeEach
    void setup() {
        when(jwtService.extractUsername(jwtToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.validateToken(jwtToken, userDetails)).thenReturn(true);
        //authenticate user
        doAnswer(invocation -> {
            UserDetails userAdapter = invocation.getArgument(0);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userAdapter,
                            null, userAdapter.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return null;
        }).when(authenticationService).authenticate(userDetails);
    }

    @Test
    @DisplayName("Test JWTFilter with PUT /update endpoint, not authenticated")
    public void testUpdateEndpoint_NotAuthenticated() throws Exception {
        var requestBody = UserDTO.builder()
                .username("username")
                .balance(2D)
                .build();

        var request = put("/api/users/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test JWTFilter with PUT /update endpoint, refreshing token")
    public void testUpdateEndpoint_RefreshingToken() throws Exception {
        UUID refreshToken = UUID.randomUUID();
        var requestBody = UserUpdateRequest.builder()
                .username(username)
                .balance(2D)
                .build();
        var userExpected = UserDTO.builder()
                .username(username)
                .balance(2D)
                .build();
        //override default behaviour defined in @BeforeEach(first throws exception, second after refresh returns username)
        when(jwtService.extractUsername(jwtToken))
                .thenThrow(new ExpiredJwtException(null, null, "Expired"))
                .thenReturn(username);
        when(refreshTokenService.isExpired(refreshToken.toString())).thenReturn(false);
        when(refreshTokenService.getUsername(refreshToken.toString())).thenReturn(username);
        when(jwtService.generateToken(username)).thenReturn(jwtToken);

        when(userService.updateUser(eq(username), any(UserDTO.class))).thenReturn(userExpected);

        var request = put("/api/users/update")
                .cookie(new Cookie(jwtCookie, jwtToken), new Cookie(jwtRefreshCookie, refreshToken.toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userExpected)));

        //verify filter calls
        verify(jwtService, times(2)).extractUsername(jwtToken);
        verify(refreshTokenService, times(1)).isExpired(refreshToken.toString());
        verify(refreshTokenService, times(1)).getUsername(refreshToken.toString());
        verify(jwtService, times(1)).generateToken(username);
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtService, times(1)).validateToken(jwtToken, userDetails);
        verify(authenticationService, times(1)).authenticate(userDetails);

        verify(userService, times(1)).updateUser(eq(username), any(UserDTO.class));
    }

    @Test
    @DisplayName("Test JWTFilter with PUT /update endpoint, impossible to refresh")
    public void testUpdateEndpoint_ImpossibleToRefresh() throws Exception {
        UUID refreshToken = UUID.randomUUID();
        var requestBody = UserUpdateRequest.builder()
                .username(username)
                .balance(2D)
                .build();
        var userExpected = UserDTO.builder()
                .username(username)
                .balance(2D)
                .build();
        //override default behaviour defined in @BeforeEach
        when(jwtService.extractUsername(jwtToken))
                .thenThrow(new ExpiredJwtException(null, null, "Expired"));
        when(refreshTokenService.isExpired(refreshToken.toString())).thenReturn(true);

        when(userService.updateUser(eq(username), any(UserDTO.class))).thenReturn(userExpected);

        var request = put("/api/users/update")
                .cookie(new Cookie(jwtCookie, jwtToken), new Cookie(jwtRefreshCookie, refreshToken.toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        assertThatThrownBy(() -> mockMvc.perform(request))
                .isInstanceOf(ServletException.class)
                .hasCause(new AccessDeniedException("Access Denied")); //because of the redirect inside the filter

        //verify filter calls
        verify(jwtService, times(1)).extractUsername(jwtToken);
        verify(refreshTokenService, times(1)).isExpired(refreshToken.toString());
        verify(refreshTokenService, times(1)).removeRefreshToken(refreshToken.toString());
        verify(authenticationService, times(2))
                .updateCookie(any(HttpServletResponse.class), any(Cookie.class), eq(0));
    }
}
