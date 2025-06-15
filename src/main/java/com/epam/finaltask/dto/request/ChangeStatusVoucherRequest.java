package com.epam.finaltask.dto.request;

import com.epam.finaltask.model.VoucherStatus;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeStatusVoucherRequest {
    @NotNull(message = "Status can not be empty")
    private VoucherStatus status;
    
}
