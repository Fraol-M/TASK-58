package com.campusfit.study.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyExportData {

    private List<StudyPlanResponse> plans;
    private List<DailyCompletionResponse> completions;
    private List<ForgettingPointResponse> forgettingPoints;
    private String exportedAt;
    private Long userId;
}
