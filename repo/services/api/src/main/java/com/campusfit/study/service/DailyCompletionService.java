package com.campusfit.study.service;

import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import com.campusfit.study.dto.DailyCompletionRequest;
import com.campusfit.study.dto.DailyCompletionResponse;
import com.campusfit.study.entity.DailyCompletion;
import com.campusfit.study.entity.StudyPlan;
import com.campusfit.study.repository.DailyCompletionRepository;
import com.campusfit.study.repository.StudyPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DailyCompletionService {

    private final DailyCompletionRepository completionRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final StreakService streakService;

    public DailyCompletionService(DailyCompletionRepository completionRepository,
                                  StudyPlanRepository studyPlanRepository,
                                  StreakService streakService) {
        this.completionRepository = completionRepository;
        this.studyPlanRepository = studyPlanRepository;
        this.streakService = streakService;
    }

    @Transactional
    public DailyCompletionResponse record(Long planId, Long userId, DailyCompletionRequest request) {
        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyPlan", planId));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to record completions for this plan");
        }

        DailyCompletion completion = DailyCompletion.builder()
                .planId(planId)
                .itemId(request.getItemId())
                .completedDate(request.getCompletedDate())
                .completed(request.getCompleted())
                .notes(request.getNotes())
                .build();

        DailyCompletion saved = completionRepository.save(completion);

        // Update streak if completed
        if (Boolean.TRUE.equals(request.getCompleted())) {
            streakService.updateStreak(userId, planId, request.getCompletedDate());
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DailyCompletionResponse> getByPlanId(Long planId, Long userId) {
        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyPlan", planId));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to view completions for this plan");
        }

        return completionRepository.findByPlanId(planId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DailyCompletionResponse toResponse(DailyCompletion c) {
        return DailyCompletionResponse.builder()
                .id(c.getId())
                .planId(c.getPlanId())
                .itemId(c.getItemId())
                .completedDate(c.getCompletedDate())
                .completed(c.isCompleted())
                .notes(c.getNotes())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
