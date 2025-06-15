package com.epam.finaltask.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

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
public class UserChangeStatusRequest {
    @Schema(example = "00000000-0000-0000-0000-000000000000")
    @Pattern(regexp="^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
            message = "Bad id!")
    private String id;
    @Schema(example = "true")
    @NotNull(message = "User's account status can not be empty")
    private boolean active;
}
