package com.campusfit.masterdata.dto;

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
public class MasterDataResponse {

    private Long id;
    private String code;
    private String name;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Term-specific
    private LocalDate startDate;
    private LocalDate endDate;

    // Major-specific
    private Long schoolId;

    // Class-specific
    private Long majorId;
    private Integer year;

    // Course-specific
    private Long classId;
    private Long termId;
    private Integer credits;
}
