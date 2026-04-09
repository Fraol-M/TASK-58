package com.campusfit.study.dto;

import com.campusfit.study.entity.StudyPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanResponse {

    private Long id;
    private Long userId;
    private Long termId;
    private Long schoolId;
    private Long majorId;
    private Long classId;
    private Long courseId;
    private String title;
    private String description;
    private StudyPlan.PlanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
