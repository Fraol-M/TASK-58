package com.campusfit.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MergeRequest {

    @NotBlank(message = "Entity type is required")
    private String entityType;

    @NotNull(message = "Source ID is required")
    private Long sourceId;

    @NotNull(message = "Target ID is required")
    private Long targetId;
}
