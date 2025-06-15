package com.epam.finaltask.viewcontroller;

import com.epam.finaltask.auth.UserAdapter;
import com.epam.finaltask.auth.bruteforce.BruteForceAuthProvider;
import com.epam.finaltask.config.ApplicationConfig;
import com.epam.finaltask.config.SecurityConfig;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.request.UserChangeStatusRequest;
import com.epam.finaltask.dto.request.UserUpdateRequest;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.restcontroller.UserRestController;
import com.epam.finaltask.restcontroller.viewscontroller.UserController;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.RefreshTokenService;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.token.JWTService;

import jakarta.servlet.http.Cookie;

import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
//importing configuration
@Import({SecurityConfig.class, ApplicationConfig.class})
@Log4j2
public class UserControllerTest {
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
    private UserRestController userRestController;

    @Qualifier("jwtCookieName")
    @Autowired
    private String jwtCookie;

    //perform authorization before every request
    String jwtToken = "jwtExample";
    String username = "username";
    UserAdapter userDetails = new UserAdapter(User.builder()
            .username(username)
            .password("123")
            .role(Role.ADMIN)
            .build());
    private Cookie jwtCookies;
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
        jwtCookies = new Cookie(jwtCookie, jwtToken);
    }

    @Test
    @DisplayName("Test GET /account endpoint")
    void testAccountEndpoint() throws Exception {
        var expect = UserDTO.builder()
                .username(username)
                .password("123")
                .role(Role.ADMIN.name())
                .balance(20D)
                .active(true)
                .build();

        when(userService.getUserByUsername(username)).thenReturn(expect);

        var result = get("/users/account")
                .cookie(jwtCookies);
        mockMvc.perform(result)
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", expect))
                .andExpect(view().name("/user/account"));
        //verify filter calls
        verify(jwtService, times(1)).extractUsername(jwtToken);
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtService, times(1)).validateToken(jwtToken, userDetails);
        verify(authenticationService, times(1)).authenticate(userDetails);

        verify(userService, times(1)).getUserByUsername(username);
    }

    @Test
    @DisplayName("Test GET /account endpoint, unauthenticated")
    void testAccountEndpoint_Unauthenticated() throws Exception {
        var result = get("/users/account");
        mockMvc.perform(result)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test GET /update endpoint")
    void testUpdatePageEndpoint() throws Exception {
        var result = get("/users/update")
                .cookie(jwtCookies);
        mockMvc.perform(result)
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", new UserUpdateRequest()))
                .andExpect(model().attribute("roles", Role.values()))
                .andExpect(view().name("/user/update"));
        //verify filter calls
        verify(jwtService, times(1)).extractUsername(jwtToken);
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtService, times(1)).validateToken(jwtToken, userDetails);
        verify(authenticationService, times(1)).authenticate(userDetails);
    }

    @Test
    @DisplayName("Test GET /change/status endpoint")
    void testChangeStatusPageEndpoint() throws Exception {
        var result = get("/users/change/status")
                .cookie(jwtCookies);
        mockMvc.perform(result)
                .andExpect(status().isOk())
                .andExpect(model().attribute("changeStatus", new UserChangeStatusRequest()))
                .andExpect(view().name("/user/change-status"));
        //verify filter calls
        verify(jwtService, times(1)).extractUsername(jwtToken);
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtService, times(1)).validateToken(jwtToken, userDetails);
        verify(authenticationService, times(1)).authenticate(userDetails);
    }

    @Test
    @DisplayName("Test POST /update endpoint")
    public void testUpdateEndpoint() throws Exception {
        var requestBody = UserUpdateRequest.builder()
                .username(username)
                .balance(2D)
                .build();
        var userExpected = UserDTO.builder()
                .username(username)
                .balance(2D)
                .build();

        when(userRestController.updateUser(requestBody, userDetails)).thenReturn(userExpected);

        var request = post("/users/update")
                .cookie(jwtCookies)
                .flashAttr("user", requestBody);
        mockMvc.perform(request)
                .andExpect(status().isFound()) //redirection
                .andExpect(model().attribute("user", userExpected))
                .andExpect(view().name("redirect:/users/account"));

        //verify filter calls
        verify(jwtService, times(1)).extractUsername(jwtToken);
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtService, times(1)).validateToken(jwtToken, userDetails);
        verify(authenticationService, times(1)).authenticate(userDetails);

        verify(userRestController, times(1)).updateUser(requestBody, userDetails);
    }

    @Test
    @DisplayName("Test POST /update endpoint, not admin trying to update another user")
    public void testUpdateEndpoint_NotAdminUpdateAnotherUser() throws Exception {
        String notAdmin = "notAdmin";
        UserAdapter userDetailsNotAdmin = new UserAdapter(User.builder()
                .username(notAdmin)
                .password("123")
                .role(Role.USER)
                .build());
        var requestBody = UserUpdateRequest.builder()
                .username(username)
                .balance(2D)
                .build();
        //overriding authorization as not admin
        when(jwtService.extractUsername(jwtToken)).thenReturn(notAdmin);
        when(userDetailsService.loadUserByUsername(notAdmin)).thenReturn(userDetailsNotAdmin);
        when(jwtService.validateToken(jwtToken, userDetailsNotAdmin)).thenReturn(true);
        //authenticate user
        doAnswer(invocation -> {
            UserDetails userAdapter = invocation.getArgument(0);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userAdapter,
                            null, userAdapter.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return null;
        }).when(authenticationService).authenticate(userDetailsNotAdmin);

        when(userRestController.updateUser(requestBody, userDetailsNotAdmin))
                .thenThrow(new BadCredentialsException("You are not allowed to update this user!"));

        var request = post("/users/update")
                .cookie(jwtCookies)
                .flashAttr("user", requestBody);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(model().attribute("exception", "You are not allowed to update this user!"))
                .andExpect(view().name("/error"));

        //verify filter calls
        verify(jwtService, times(1)).extractUsername(jwtToken);
        verify(userDetailsService, times(1)).loadUserByUsername(notAdmin);
        verify(jwtService, times(1)).validateToken(jwtToken, userDetailsNotAdmin);
        verify(authenticationService, times(1)).authenticate(userDetailsNotAdmin);

        verify(userRestController, times(1)).updateUser(requestBody, userDetailsNotAdmin);
    }

    @Test
    @DisplayName("Test POST /change/status endpoint")
    public void testChangeStatusEndpoint() throws Exception {
        UUID uuid = UUID.randomUUID();
        var requestBody = UserChangeStatusRequest.builder()
                .id(uuid.toString())
                .active(true)
                .build();
        var userExpected = UserDTO.builder()
                .id(uuid.toString())
                .username("user")
                .balance(2D)
                .active(true)
                .build();

        when(userRestController.changeStatus(requestBody, userDetails)).thenReturn(userExpected);

        var request = post("/users/change/status")
                .cookie(jwtCookies)
                .flashAttr("changeStatus", requestBody);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", userExpected))
                .andExpect(view().name("/user/account"));

        //verify filter calls
        verify(jwtService, times(1)).extractUsername(jwtToken);
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtService, times(1)).validateToken(jwtToken, userDetails);
        verify(authenticationService, times(1)).authenticate(userDetails);

        verify(userRestController, times(1)).changeStatus(requestBody, userDetails);
    }
}
