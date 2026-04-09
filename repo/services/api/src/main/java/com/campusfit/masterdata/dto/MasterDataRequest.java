package com.campusfit.masterdata.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
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
public class MasterDataRequest {

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Effective from date is required")
    @JsonFormat(pattern = "MM/dd/yyyy")
    private LocalDate effectiveFrom;

    @JsonFormat(pattern = "MM/dd/yyyy")
    private LocalDate effectiveTo;

    // Term-specific fields
    @JsonFormat(pattern = "MM/dd/yyyy")
    private LocalDate startDate;
    @JsonFormat(pattern = "MM/dd/yyyy")
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
