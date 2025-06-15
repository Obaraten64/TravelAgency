package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.request.UserChangeStatusRequest;
import com.epam.finaltask.dto.request.UserUpdateRequest;
import com.epam.finaltask.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/users")
@RequiredArgsConstructor
@Log4j2
@SecurityRequirement(name = "Bearer Authentication")
public class UserRestController {
    private final UserService userService;

    @Operation(summary = "Update user")
    @ApiResponse(responseCode = "200",
            description = "Update user's data",
            content = @Content(
                    schema = @Schema(implementation = UserDTO.class),
                    examples = @ExampleObject(
                            value = "{\"username\": \"vadimliakh\"," +
                                    "\"role\": \"user\", \"phoneNumber\": \"0933903939\"," +
                                    "\"balance\":35.35,\"active\":true}")))
    @PutMapping("/update")
    public UserDTO updateUser(@RequestBody @Valid UserUpdateRequest userUpdateRequest,
                                   @AuthenticationPrincipal UserDetails user) {
        log.info("User, {}, data update request from: {} ",
                userUpdateRequest.getUsername(), user.getUsername());
        UserDTO registrationUserDTO = UserDTO.builder()
                .username(userUpdateRequest.getUsername())
                .password(userUpdateRequest.getPassword())
                .role(userUpdateRequest.getRole())
                .phoneNumber(userUpdateRequest.getPhoneNumber())
                .balance(userUpdateRequest.getBalance())
                .build();

        UserDTO userDTO = userService.updateUser(user.getUsername(), registrationUserDTO);

        log.info("User updated: {}", userDTO.getUsername());
        return userDTO;
    }
    @Operation(summary = "Update user's account status, \"ROLE_ADMIN\" role required")
    @ApiResponse(responseCode = "200",
            description = "Update user's account status",
            content = @Content(
                    schema = @Schema(implementation = UserDTO.class),
                    examples = @ExampleObject(
                            value = "{\"username\": \"vadimliakh\"," +
                                    "\"role\": \"user\", \"phoneNumber\": \"0933903939\"," +
                                    "\"balance\":35.35,\"active\":false}")))
    @PatchMapping("/change/status")
    public UserDTO changeStatus(@RequestBody @Valid UserChangeStatusRequest userChangeStatusRequest,
                                     @AuthenticationPrincipal UserDetails user) {
        log.info("Account status change request from: {} ", user.getUsername());
        UserDTO registrationUserDTO = UserDTO.builder()
                .id(userChangeStatusRequest.getId())
                .active(userChangeStatusRequest.isActive())
                .build();

        UserDTO userDTO = userService.changeAccountStatus(registrationUserDTO);

        log.info("User status updated: {}", userDTO.getUsername());
        return userDTO;
    }
}
