package com.campusfit.fitness.dto;

import com.campusfit.fitness.entity.Goal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {

    private Long id;
    private Long userId;
    private Long assessmentId;
    private Goal.GoalType goalType;
    private String description;
    private BigDecimal targetValue;
    private BigDecimal startValue;
    private BigDecimal currentValue;
    private String unit;
    private LocalDate startDate;
    private LocalDate targetDate;
    private Goal.GoalStatus status;
    private int missedCheckIns;
    private double progressPercentage;
    private List<MilestoneResponse> milestones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneResponse {
        private Long id;
        private String description;
        private BigDecimal targetValue;
        private LocalDate achievedDate;
        private int seq;
    }
}
