package com.campusfit.export_.dto;

import com.campusfit.export_.entity.ExportJob;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {

    @NotNull(message = "Export type is required")
    private ExportJob.ExportType exportType;

    private boolean passwordProtected;

    private String exportPassword;
}
