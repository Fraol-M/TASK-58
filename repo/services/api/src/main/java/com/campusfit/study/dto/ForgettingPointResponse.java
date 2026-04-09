package com.campusfit.study.dto;

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
public class ForgettingPointResponse {

    private Long id;
    private Long planId;
    private String topic;
    private String description;
    private LocalDate nextReviewDate;
    private BigDecimal easeFactor;
    private int intervalDays;
    private int repetitions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
