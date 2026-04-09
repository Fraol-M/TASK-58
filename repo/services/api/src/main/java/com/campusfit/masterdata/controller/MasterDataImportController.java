package com.campusfit.masterdata.controller;

import com.campusfit.masterdata.dto.ImportJobResponse;
import com.campusfit.masterdata.service.MasterDataImportService;
import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/master-data/imports")
public class MasterDataImportController {

    private final MasterDataImportService importService;

    public MasterDataImportController(MasterDataImportService importService) {
        this.importService = importService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImportJobResponse>> importFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        ImportJobResponse response = importService.processImport(file, entityType, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ImportJobResponse>> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(importService.getJobById(id)));
    }
}
