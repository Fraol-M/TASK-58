package com.campusfit.export_.dto;

import com.campusfit.export_.entity.ExportJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResponse {

    private Long id;
    private Long userId;
    private ExportJob.ExportType exportType;
    private ExportJob.JobStatus status;
    private boolean downloadReady;
    private boolean passwordProtected;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
