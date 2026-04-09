package com.campusfit.fitness.controller;

import com.campusfit.fitness.dto.CheckInRequest;
import com.campusfit.fitness.dto.CheckInResponse;
import com.campusfit.fitness.service.CheckInService;
import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fitness/goals/{goalId}/check-ins")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CheckInResponse>> create(
            @PathVariable Long goalId,
            @Valid @RequestBody CheckInRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        CheckInResponse response = checkInService.create(goalId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CheckInResponse>>> getAll(@PathVariable Long goalId) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        List<CheckInResponse> responses = checkInService.getByGoalId(goalId, userId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
