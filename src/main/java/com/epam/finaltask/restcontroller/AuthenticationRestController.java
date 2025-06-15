package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.request.UserLoginRequest;
import com.epam.finaltask.dto.request.UserRegistrationRequest;
import com.epam.finaltask.dto.Tokens;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/auth")
@RequiredArgsConstructor
@Log4j2
public class AuthenticationRestController {
    // http://localhost:8080/swagger-ui/index.html to access swagger
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "Register user")
    @ApiResponse(responseCode = "200",
            description = "Data about registered user",
            content = @Content(
                    schema = @Schema(implementation = UserDTO.class),
                    examples = @ExampleObject(
                            value = "{\"username\": \"vadimliakh\"," +
                                    "\"role\": \"user\", \"phoneNumber\": \"0933903939\"," +
                                    "\"balance\":35.35,\"active\":true}")))
    @PostMapping("/register")
    public UserDTO registerUser(@RequestBody @Valid UserRegistrationRequest userRegistrationRequest) {
        log.info("Registration request from: {} ", userRegistrationRequest.getUsername());
        UserDTO registrationUserDTO = UserDTO.builder()
                .username(userRegistrationRequest.getUsername())
                .password(userRegistrationRequest.getPassword())
                .role(userRegistrationRequest.getRole())
                .phoneNumber(userRegistrationRequest.getPhoneNumber())
                .balance(userRegistrationRequest.getBalance())
                .build();

        UserDTO userDTO = userService.register(registrationUserDTO);

        log.info("User registered: {}", userDTO.getUsername());
        return userDTO;
    }
    @Operation(summary = "Login into application")
    @ApiResponse(responseCode = "200",
            description = "JWT token and refresh that needed for authorization",
            content = @Content(
                    schema = @Schema(implementation = Tokens.class),
                    examples = @ExampleObject(
                            value = "{\"jwtToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30\"," +
                                    "\"refreshToken\": \"00000000-0000-0000-0000-000000000000\"}")))
    @PostMapping("/login")
    public Tokens login(@RequestBody @Valid UserLoginRequest userLoginRequest) {
        log.info("Login request from: {} ", userLoginRequest.getUsername());
        //authenticate user
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userLoginRequest.getUsername(), userLoginRequest.getPassword()));
        //create tokens
        Tokens tokens = authenticationService.generateToken(userLoginRequest.getUsername());

        log.info("Login successful from: {} ", userLoginRequest.getUsername());
        return tokens;
    }
    @Operation(summary = "Logout from application")
    @ApiResponse(responseCode = "200",
            description = "Logout from application",
            content = @Content)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(Tokens tokens) {
        log.info("Logout request");
        //delete tokens
        authenticationService.deleteRefreshToken(tokens);
        //authenticate user as anonymous
        authenticationService.authenticateAnonymous();
        log.info("Logout successful");
        return new ResponseEntity<>("Logout successfully", HttpStatus.OK);
    }
}
