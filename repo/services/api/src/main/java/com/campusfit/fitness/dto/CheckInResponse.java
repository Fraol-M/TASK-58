package com.campusfit.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponse {

    private Long id;
    private Long goalId;
    private Long userId;
    private int weekNumber;
    private BigDecimal value;
    private String notes;
    private LocalDateTime createdAt;
}
