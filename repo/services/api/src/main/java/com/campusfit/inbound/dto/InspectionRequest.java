package com.campusfit.inbound.dto;

import com.campusfit.inbound.entity.InboundLine;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionRequest {

    @NotNull(message = "Line ID is required")
    private Long lineId;

    @NotNull(message = "Inspected quantity is required")
    private BigDecimal inspectedQty;

    @NotNull(message = "Inspection result is required")
    private InboundLine.InspectionResult result;

    private String notes;
}
