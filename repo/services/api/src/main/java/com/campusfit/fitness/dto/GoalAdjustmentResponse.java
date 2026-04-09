package com.campusfit.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalAdjustmentResponse {

    private Long id;
    private Long goalId;
    private BigDecimal previousTarget;
    private BigDecimal newTarget;
    private LocalDate previousTargetDate;
    private LocalDate newTargetDate;
    private String reason;
    private Long adjustedBy;
    private LocalDateTime createdAt;
}
