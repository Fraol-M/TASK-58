package com.campusfit.inbound.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveLineRequest {

    @NotNull(message = "Line ID is required")
    private Long lineId;

    @NotNull(message = "Received quantity is required")
    @PositiveOrZero(message = "Received quantity must be zero or positive")
    private BigDecimal receivedQty;
}
