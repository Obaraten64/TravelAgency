package com.epam.finaltask.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeHotStatusVoucherRequest {
    private Boolean isHot = false;
}
