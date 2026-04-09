package com.campusfit.inbound.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PutawayRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    private String actualLocation;
}
