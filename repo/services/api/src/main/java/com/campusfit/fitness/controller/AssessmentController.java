package com.campusfit.fitness.controller;

import com.campusfit.fitness.dto.AssessmentRequest;
import com.campusfit.fitness.dto.AssessmentResponse;
import com.campusfit.fitness.service.AssessmentService;
import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fitness")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping("/assessment")
    public ResponseEntity<ApiResponse<AssessmentResponse>> getLatest() {
        Long userId = SecurityContextHelper.getCurrentUserId();
        AssessmentResponse response = assessmentService.getLatest(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/assessment")
    public ResponseEntity<ApiResponse<AssessmentResponse>> createOrUpdate(
            @Valid @RequestBody AssessmentRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        AssessmentResponse response = assessmentService.createOrUpdate(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
