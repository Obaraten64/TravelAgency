package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tokens {
    @NotNull(message = "Your JWT token can't be empty")
    private String jwtToken;
    @NotNull(message = "Your refresh token can't be empty")
    private String refreshToken;

}
