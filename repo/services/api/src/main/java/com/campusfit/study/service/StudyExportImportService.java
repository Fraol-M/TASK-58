package com.campusfit.study.service;

import com.campusfit.shared.exception.BusinessException;
import com.campusfit.study.dto.*;
import com.campusfit.study.entity.DailyCompletion;
import com.campusfit.study.entity.ForgettingPoint;
import com.campusfit.study.entity.StudyPlan;
import com.campusfit.study.repository.DailyCompletionRepository;
import com.campusfit.study.repository.ForgettingPointRepository;
import com.campusfit.study.repository.StudyPlanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudyExportImportService {

    private final StudyPlanRepository studyPlanRepository;
    private final DailyCompletionRepository completionRepository;
    private final ForgettingPointRepository forgettingPointRepository;
    private final ObjectMapper objectMapper;

    public StudyExportImportService(StudyPlanRepository studyPlanRepository,
                                    DailyCompletionRepository completionRepository,
                                    ForgettingPointRepository forgettingPointRepository) {
        this.studyPlanRepository = studyPlanRepository;
        this.completionRepository = completionRepository;
        this.forgettingPointRepository = forgettingPointRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional(readOnly = true)
    public StudyExportData exportData(Long userId) {
        List<StudyPlan> plans = studyPlanRepository.findByUserId(userId);

        List<StudyPlanResponse> planResponses = plans.stream()
                .map(this::toPlanResponse)
                .collect(Collectors.toList());

        List<DailyCompletionResponse> completionResponses = new ArrayList<>();
        List<ForgettingPointResponse> fpResponses = new ArrayList<>();

        for (StudyPlan plan : plans) {
            completionRepository.findByPlanId(plan.getId()).stream()
                    .map(this::toCompletionResponse)
                    .forEach(completionResponses::add);

            forgettingPointRepository.findByPlanId(plan.getId()).stream()
                    .map(this::toFpResponse)
                    .forEach(fpResponses::add);
        }

        return StudyExportData.builder()
                .userId(userId)
                .plans(planResponses)
                .completions(completionResponses)
                .forgettingPoints(fpResponses)
                .exportedAt(LocalDateTime.now().toString())
                .build();
    }

    @Transactional
    public String importData(Long userId, StudyExportData data) {
        if (data.getPlans() == null || data.getPlans().isEmpty()) {
            throw new BusinessException("No study plans found in import data");
        }

        int importedPlans = 0;
        int importedCompletions = 0;
        int importedForgettingPoints = 0;

        for (StudyPlanResponse planData : data.getPlans()) {
            Long oldPlanId = planData.getId();

            StudyPlan plan = StudyPlan.builder()
                    .userId(userId)
                    .courseId(planData.getCourseId())
                    .termId(planData.getTermId())
                    .title(planData.getTitle())
                    .description(planData.getDescription())
                    .status(StudyPlan.PlanStatus.ACTIVE)
                    .build();
            plan = studyPlanRepository.save(plan);
            importedPlans++;

            // Import completions for this plan
            if (data.getCompletions() != null) {
                for (DailyCompletionResponse comp : data.getCompletions()) {
                    if (comp.getPlanId() != null && comp.getPlanId().equals(oldPlanId)) {
                        DailyCompletion completion = DailyCompletion.builder()
                                .planId(plan.getId())
                                .itemId(comp.getItemId())
                                .completedDate(comp.getCompletedDate())
                                .completed(comp.isCompleted())
                                .notes(comp.getNotes())
                                .build();
                        completionRepository.save(completion);
                        importedCompletions++;
                    }
                }
            }

            // Import forgetting points for this plan
            if (data.getForgettingPoints() != null) {
                for (ForgettingPointResponse fp : data.getForgettingPoints()) {
                    if (fp.getPlanId() != null && fp.getPlanId().equals(oldPlanId)) {
                        ForgettingPoint point = ForgettingPoint.builder()
                                .planId(plan.getId())
                                .topic(fp.getTopic())
                                .description(fp.getDescription())
                                .nextReviewDate(fp.getNextReviewDate())
                                .easeFactor(fp.getEaseFactor())
                                .intervalDays(fp.getIntervalDays())
                                .repetitions(fp.getRepetitions())
                                .build();
                        forgettingPointRepository.save(point);
                        importedForgettingPoints++;
                    }
                }
            }
        }

        return String.format("Successfully imported %d plans, %d completions, %d forgetting points",
                importedPlans, importedCompletions, importedForgettingPoints);
    }

    public String toJson(StudyExportData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Failed to serialize export data: " + e.getMessage());
        }
    }

    public StudyExportData fromJson(String json) {
        try {
            return objectMapper.readValue(json, StudyExportData.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Failed to parse import data: " + e.getMessage());
        }
    }

    private StudyPlanResponse toPlanResponse(StudyPlan plan) {
        return StudyPlanResponse.builder()
                .id(plan.getId())
                .userId(plan.getUserId())
                .courseId(plan.getCourseId())
                .termId(plan.getTermId())
                .title(plan.getTitle())
                .description(plan.getDescription())
                .status(plan.getStatus())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    private DailyCompletionResponse toCompletionResponse(DailyCompletion c) {
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

    private ForgettingPointResponse toFpResponse(ForgettingPoint fp) {
        return ForgettingPointResponse.builder()
                .id(fp.getId())
                .planId(fp.getPlanId())
                .topic(fp.getTopic())
                .description(fp.getDescription())
                .nextReviewDate(fp.getNextReviewDate())
                .easeFactor(fp.getEaseFactor())
                .intervalDays(fp.getIntervalDays())
                .repetitions(fp.getRepetitions())
                .createdAt(fp.getCreatedAt())
                .updatedAt(fp.getUpdatedAt())
                .build();
    }
}
