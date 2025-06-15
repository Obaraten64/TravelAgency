package com.epam.finaltask.dto.request;

import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateVoucherRequest {

    private String title;

    private String description;

    private Double price;

    private String tourType;

    private String transferType;

    private String hotelType;

    private String status;

    private LocalDate arrivalDate;

    private LocalDate evictionDate;

    @Pattern(regexp="^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
            message = "Bad user id!")
    private String userId;

    private Boolean isHot;
    
}
