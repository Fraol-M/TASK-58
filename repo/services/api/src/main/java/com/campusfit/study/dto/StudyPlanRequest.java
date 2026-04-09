package com.campusfit.study.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Long termId;

    private Long schoolId;

    private Long majorId;

    private Long classId;

    private Long courseId;
}
