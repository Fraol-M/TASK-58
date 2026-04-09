package com.campusfit.study.controller;

import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import com.campusfit.study.dto.StudyExportData;
import com.campusfit.study.service.StudyExportImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study")
public class StudyExportController {

    private final StudyExportImportService exportImportService;

    public StudyExportController(StudyExportImportService exportImportService) {
        this.exportImportService = exportImportService;
    }

    @GetMapping("/export")
    public ResponseEntity<ApiResponse<StudyExportData>> export() {
        Long userId = SecurityContextHelper.getCurrentUserId();
        StudyExportData data = exportImportService.exportData(userId);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<String>> importData(@RequestBody StudyExportData data) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        String result = exportImportService.importData(userId, data);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
