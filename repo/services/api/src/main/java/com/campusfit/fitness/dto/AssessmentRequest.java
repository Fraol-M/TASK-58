package com.campusfit.fitness.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentRequest {

    @NotNull(message = "Height (feet) is required")
    @Min(value = 1, message = "Height feet must be at least 1")
    @Max(value = 8, message = "Height feet must be at most 8")
    private Integer heightFeet;

    @NotNull(message = "Height (inches) is required")
    @Min(value = 0, message = "Height inches must be at least 0")
    @Max(value = 11, message = "Height inches must be at most 11")
    private Integer heightInches;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weightLbs;

    private Double bodyFatPercent;

    private Double waist;

    private Double chest;

    private Double arm;

    private String notes;
}
