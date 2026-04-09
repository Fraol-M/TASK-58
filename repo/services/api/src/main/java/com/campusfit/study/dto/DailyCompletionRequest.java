package com.campusfit.study.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCompletionRequest {

    private Long itemId;

    @NotNull(message = "Completed date is required")
    private LocalDate completedDate;

    @NotNull(message = "Completed flag is required")
    private Boolean completed;

    private String notes;
}
