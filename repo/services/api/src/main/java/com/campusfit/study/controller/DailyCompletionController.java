package com.campusfit.study.controller;

import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import com.campusfit.study.dto.DailyCompletionRequest;
import com.campusfit.study.dto.DailyCompletionResponse;
import com.campusfit.study.service.DailyCompletionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study/plans/{planId}/completions")
public class DailyCompletionController {

    private final DailyCompletionService completionService;

    public DailyCompletionController(DailyCompletionService completionService) {
        this.completionService = completionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DailyCompletionResponse>> record(
            @PathVariable Long planId,
            @Valid @RequestBody DailyCompletionRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        DailyCompletionResponse response = completionService.record(planId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DailyCompletionResponse>>> getAll(@PathVariable Long planId) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        List<DailyCompletionResponse> responses = completionService.getByPlanId(planId, userId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
