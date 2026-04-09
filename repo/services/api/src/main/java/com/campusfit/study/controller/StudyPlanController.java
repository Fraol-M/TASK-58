package com.campusfit.study.controller;

import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import com.campusfit.study.dto.StudyPlanRequest;
import com.campusfit.study.dto.StudyPlanResponse;
import com.campusfit.study.service.StudyPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/api/study/plans")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    public StudyPlanController(StudyPlanService studyPlanService) {
        this.studyPlanService = studyPlanService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudyPlanResponse>> create(@Valid @RequestBody StudyPlanRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        StudyPlanResponse response = studyPlanService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<StudyPlanResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        Page<StudyPlanResponse> paged = studyPlanService.getAllForUser(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(paged));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudyPlanResponse>> getById(@PathVariable Long id) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        StudyPlanResponse response = studyPlanService.getById(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
