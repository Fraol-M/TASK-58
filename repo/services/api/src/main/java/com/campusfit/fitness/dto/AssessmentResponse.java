package com.campusfit.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResponse {

    private Long id;
    private Long userId;
    private String assessmentType;
    private int heightFeet;
    private int heightInches;
    private String formattedHeight;
    private double weightLbs;
    private Double bodyFatPercent;
    private Double waistInches;
    private Double chestInches;
    private Double armInches;
    private LocalDate assessmentDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
