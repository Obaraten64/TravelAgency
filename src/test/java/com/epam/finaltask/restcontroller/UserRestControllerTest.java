package com.epam.finaltask.restcontroller;

import com.epam.finaltask.auth.UserAdapter;
import com.epam.finaltask.auth.bruteforce.BruteForceAuthProvider;
import com.epam.finaltask.config.ApplicationConfig;
import com.epam.finaltask.config.SecurityConfig;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.request.UserChangeStatusRequest;
import com.epam.finaltask.dto.request.UserUpdateRequest;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.RefreshTokenService;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.token.JWTService;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRestController.class)
//importing configuration
@Import({SecurityConfig.class, ApplicationConfig.class})
@Log4j2
public class UserRestControllerTest {
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
    @DisplayName("Test PUT /update endpoint")
    public void testUpdateEndpoint() throws Exception {
        var requestBody = UserUpdateRequest.builder()
                .username(username)
                .balance(2D)
                .build();
        var userExpected = UserDTO.builder()
                .username(username)
                .balance(2D)
                .build();

        when(userService.updateUser(eq(username), any(UserDTO.class))).thenReturn(userExpected);

        var request = put("/api/users/update")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userExpected)));

        //verify filter calls
        verify(jwtService, times(1)).extractUsername(jwtToken);
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtService, times(1)).validateToken(jwtToken, userDetails);
        verify(authenticationService, times(1)).authenticate(userDetails);

        verify(userService, times(1)).updateUser(eq(username), any(UserDTO.class));
    }

    @Test
    @DisplayName("Test PUT /update endpoint, not admin trying to update another user")
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

        when(userService.updateUser(eq(notAdmin), any(UserDTO.class)))
                .thenThrow(new BadCredentialsException("You are not allowed to update this user!"));

        var request = put("/api/users/update")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exception\":\"You are not allowed to update this user!\"}"));

        //verify filter calls
        verify(jwtService, times(1)).extractUsername(jwtToken);
        verify(userDetailsService, times(1)).loadUserByUsername(notAdmin);
        verify(jwtService, times(1)).validateToken(jwtToken, userDetailsNotAdmin);
        verify(authenticationService, times(1)).authenticate(userDetailsNotAdmin);

        verify(userService, times(1)).updateUser(eq(notAdmin), any(UserDTO.class));
    }

    @Test
    @DisplayName("Test PATCH /change/status endpoint")
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

        when(userService.changeAccountStatus(any(UserDTO.class))).thenReturn(userExpected);

        var request = patch("/api/users/change/status")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userExpected)));

        //verify filter calls
        verify(jwtService, times(1)).extractUsername(jwtToken);
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtService, times(1)).validateToken(jwtToken, userDetails);
        verify(authenticationService, times(1)).authenticate(userDetails);

        verify(userService, times(1)).changeAccountStatus(any(UserDTO.class));
    }

    @Test
    @DisplayName("Test PATCH /change/status endpoint, unauthorized to perform action")
    public void testChangeStatusEndpoint_Unauthorized() throws Exception {
        String notAdmin = "notAdmin";
        UserAdapter userDetailsNotAdmin = new UserAdapter(User.builder()
                .username(notAdmin)
                .password("123")
                .role(Role.USER)
                .build());
        var requestBody = UserChangeStatusRequest.builder()
                .id(UUID.randomUUID().toString())
                .active(true)
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

        var request = patch("/api/users/change/status")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isForbidden());

        //verify filter calls
        verify(jwtService, times(1)).extractUsername(jwtToken);
        verify(userDetailsService, times(1)).loadUserByUsername(notAdmin);
        verify(jwtService, times(1)).validateToken(jwtToken, userDetailsNotAdmin);
        verify(authenticationService, times(1)).authenticate(userDetailsNotAdmin);
    }
}
