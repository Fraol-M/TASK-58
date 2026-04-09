package com.campusfit.inbound.dto;

import com.campusfit.inbound.entity.Discrepancy;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupervisorReviewRequest {

    @NotNull(message = "Discrepancy ID is required")
    private Long discrepancyId;

    @NotNull(message = "Reason code is required")
    private Discrepancy.ReasonCode reasonCode;

    private String notes;
}
