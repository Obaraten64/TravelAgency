package com.epam.finaltask.dto.request;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateVoucherRequest {
    @NotNull(message = "Title can not be empty")
    private String title;
    @NotNull(message = "Description can not be empty")
    private String description;
    @NotNull(message = "Price can not be empty")
    private Double price;
    @NotNull(message = "Tour type can not be empty")
    private String tourType;
    @NotNull(message = "Type of movement can not be empty")
    private String transferType;
    @NotNull(message = "Hotel type can not be empty")
    private String hotelType;
    @NotNull(message = "Tour status can not be empty")
    private String status;
    @NotNull(message = "Arrival date can not be empty")
    private LocalDate arrivalDate;
    @NotNull(message = "Eviction date can not be empty")
    private LocalDate evictionDate;
    @NotNull(message = "Hot status can not be empty")
    private Boolean isHot;
    
}
