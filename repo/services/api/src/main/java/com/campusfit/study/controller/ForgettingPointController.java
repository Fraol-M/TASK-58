package com.campusfit.study.controller;

import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import com.campusfit.study.dto.ForgettingPointRequest;
import com.campusfit.study.dto.ForgettingPointResponse;
import com.campusfit.study.dto.ReviewRequest;
import com.campusfit.study.service.ForgettingPointService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study")
public class ForgettingPointController {

    private final ForgettingPointService forgettingPointService;

    public ForgettingPointController(ForgettingPointService forgettingPointService) {
        this.forgettingPointService = forgettingPointService;
    }

    @PostMapping("/plans/{planId}/forgetting-points")
    public ResponseEntity<ApiResponse<ForgettingPointResponse>> create(
            @PathVariable Long planId,
            @Valid @RequestBody ForgettingPointRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        ForgettingPointResponse response = forgettingPointService.create(planId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/plans/{planId}/forgetting-points")
    public ResponseEntity<ApiResponse<List<ForgettingPointResponse>>> getByPlan(@PathVariable Long planId) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        List<ForgettingPointResponse> responses = forgettingPointService.getByPlanId(planId, userId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping("/forgetting-points/{id}/review")
    public ResponseEntity<ApiResponse<ForgettingPointResponse>> review(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        ForgettingPointResponse response = forgettingPointService.review(id, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
