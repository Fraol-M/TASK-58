package com.campusfit.masterdata.dto;

import com.campusfit.masterdata.entity.ImportJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportJobResponse {

    private Long id;
    private String fileName;
    private String entityType;
    private int totalRows;
    private int successCount;
    private int errorCount;
    private ImportJob.JobStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private List<ImportErrorResponse> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportErrorResponse {
        private int rowNumber;
        private String field;
        private String message;
        private String rawValue;
    }
}
