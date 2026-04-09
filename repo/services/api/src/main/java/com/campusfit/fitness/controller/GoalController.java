package com.campusfit.fitness.controller;

import com.campusfit.fitness.dto.GoalRequest;
import com.campusfit.fitness.dto.GoalResponse;
import com.campusfit.fitness.service.GoalService;
import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import java.util.List;

@RestController
@RequestMapping("/api/fitness/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> create(@Valid @RequestBody GoalRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        GoalResponse response = goalService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GoalResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        Page<GoalResponse> responses = goalService.getAllForUser(userId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> getById(@PathVariable Long id) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        GoalResponse response = goalService.getById(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
