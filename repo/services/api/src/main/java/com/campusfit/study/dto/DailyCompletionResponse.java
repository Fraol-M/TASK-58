package com.campusfit.study.dto;

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
public class DailyCompletionResponse {

    private Long id;
    private Long planId;
    private Long itemId;
    private LocalDate completedDate;
    private boolean completed;
    private String notes;
    private LocalDateTime createdAt;
}
