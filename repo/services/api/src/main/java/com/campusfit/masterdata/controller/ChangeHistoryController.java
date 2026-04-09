package com.campusfit.masterdata.controller;

import com.campusfit.masterdata.dto.ChangeHistoryResponse;
import com.campusfit.masterdata.service.ChangeHistoryService;
import com.campusfit.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/master-data/history")
public class ChangeHistoryController {

    private final ChangeHistoryService changeHistoryService;

    public ChangeHistoryController(ChangeHistoryService changeHistoryService) {
        this.changeHistoryService = changeHistoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChangeHistoryResponse>>> getHistory(
            @RequestParam String entityType,
            @RequestParam(required = false) Long entityId) {
        List<ChangeHistoryResponse> history;
        if (entityId != null) {
            history = changeHistoryService.getByEntity(entityType, entityId);
        } else {
            history = changeHistoryService.getByEntityType(entityType);
        }
        return ResponseEntity.ok(ApiResponse.ok(history));
    }
}
