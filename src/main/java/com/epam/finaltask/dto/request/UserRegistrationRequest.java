package com.epam.finaltask.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationRequest {
    @Schema(example = "vadimliakh")
    @NotNull(message = "Username can't be empty")
    private String username;
    @Schema(example = "Some9assword!")
    @NotNull(message = "Password can't be empty")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$",
            message = "Password must contain at least one digit, one lowercase letter, one uppercase letter," +
                    " one special character(!@#&()–[{}]:;',?/*~$^+=<>), and be between 8 and 20 characters")
    private String password;
    @Schema(example = "user")
    @NotNull(message = "Role can't be empty")
    private String role;
    @Schema(example = "0933903939")
    @NotNull(message = "Phone number can't be empty")
    private String phoneNumber;
    @Schema(example = "35.35")
    @NotNull(message = "Your balance can't be empty")
    @Min(value = 1, message = "Your balance can't be less than 1")
    private Double balance;
}
