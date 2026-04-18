package com.campusfit.fitness.service;

import com.campusfit.fitness.dto.GoalRequest;
import com.campusfit.fitness.dto.GoalResponse;
import com.campusfit.fitness.entity.Assessment;
import com.campusfit.fitness.entity.Goal;
import com.campusfit.fitness.entity.Milestone;
import com.campusfit.fitness.repository.AssessmentRepository;
import com.campusfit.fitness.repository.GoalRepository;
import com.campusfit.fitness.repository.MilestoneRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoalService {

    private static final Logger log = LoggerFactory.getLogger(GoalService.class);
    private static final int DEFAULT_MILESTONE_COUNT = 4;

    private final GoalRepository goalRepository;
    private final MilestoneRepository milestoneRepository;
    private final AssessmentRepository assessmentRepository;
    private final ObjectMapper objectMapper;

    public GoalService(GoalRepository goalRepository,
                       MilestoneRepository milestoneRepository,
                       AssessmentRepository assessmentRepository) {
        this.goalRepository = goalRepository;
        this.milestoneRepository = milestoneRepository;
        this.assessmentRepository = assessmentRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public GoalResponse create(Long userId, GoalRequest request) {
        // Resolve baseline from latest assessment
        BigDecimal startValue = BigDecimal.ZERO;
        Long assessmentId = null;
        Assessment latestAssessment = assessmentRepository
                .findTopByUserIdOrderByAssessmentDateDescIdDesc(userId).orElse(null);
        if (latestAssessment != null) {
            assessmentId = latestAssessment.getId();
            startValue = resolveBaselineFromAssessment(latestAssessment, request.getGoalType());
        }

        Goal goal = Goal.builder()
                .userId(userId)
                .assessmentId(assessmentId)
                .goalType(request.getGoalType())
                .description(request.getDescription())
                .targetValue(request.getTargetValue())
                .startValue(startValue)
                .currentValue(startValue)
                .unit(request.getUnit())
                .startDate(request.getStartDate())
                .targetDate(request.getTargetDate())
                .status(Goal.GoalStatus.ACTIVE)
                .missedCheckIns(0)
                .metricsEncrypted("{\"targetValue\":" + request.getTargetValue() + ",\"startValue\":" + startValue + "}")
                .build();

        Goal saved = goalRepository.save(goal);

        // Generate milestones evenly spaced across goal period
        generateMilestones(saved);

        List<Milestone> milestones = milestoneRepository.findByGoalIdOrderBySeq(saved.getId());
        return toResponse(saved, milestones);
    }

    @Transactional
    public GoalResponse update(Long goalId, Long userId, GoalRequest request) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));

        if (!goal.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to update this goal");
        }

        goal.setGoalType(request.getGoalType());
        goal.setDescription(request.getDescription());
        goal.setTargetValue(request.getTargetValue());
        goal.setUnit(request.getUnit());
        goal.setStartDate(request.getStartDate());
        goal.setTargetDate(request.getTargetDate());

        Goal saved = goalRepository.save(goal);
        List<Milestone> milestones = milestoneRepository.findByGoalIdOrderBySeq(saved.getId());
        return toResponse(saved, milestones);
    }

    @Transactional(readOnly = true)
    public GoalResponse getById(Long goalId, Long userId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));

        if (!goal.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to view this goal");
        }

        List<Milestone> milestones = milestoneRepository.findByGoalIdOrderBySeq(goalId);
        return toResponse(goal, milestones);
    }

    @Transactional(readOnly = true)
    public Page<GoalResponse> getAllForUser(Long userId, int page, int size) {
        Page<Goal> goals = goalRepository.findByUserId(userId, PageRequest.of(page, size));
        return goals.map(g -> {
            List<Milestone> milestones = milestoneRepository.findByGoalIdOrderBySeq(g.getId());
            return toResponse(g, milestones);
        });
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> getAllForUser(Long userId) {
        List<Goal> goals = goalRepository.findByUserId(userId);
        return goals.stream()
                .map(g -> {
                    List<Milestone> milestones = milestoneRepository.findByGoalIdOrderBySeq(g.getId());
                    return toResponse(g, milestones);
                })
                .collect(Collectors.toList());
    }

    private void generateMilestones(Goal goal) {
        long totalDays = ChronoUnit.DAYS.between(goal.getStartDate(), goal.getTargetDate());
        if (totalDays <= 0) {
            return;
        }

        BigDecimal totalChange = goal.getTargetValue().subtract(goal.getStartValue());
        int milestoneCount = DEFAULT_MILESTONE_COUNT;
        List<Milestone> milestones = new ArrayList<>();

        for (int i = 1; i <= milestoneCount; i++) {
            BigDecimal fraction = BigDecimal.valueOf(i).divide(BigDecimal.valueOf(milestoneCount), 4, RoundingMode.HALF_UP);
            BigDecimal milestoneValue = goal.getStartValue().add(
                    totalChange.multiply(fraction).setScale(2, RoundingMode.HALF_UP)
            );

            Milestone milestone = Milestone.builder()
                    .goalId(goal.getId())
                    .description("Milestone " + i + " of " + milestoneCount)
                    .targetValue(milestoneValue)
                    .seq(i)
                    .build();
            milestones.add(milestone);
        }

        milestoneRepository.saveAll(milestones);
    }

    private GoalResponse toResponse(Goal goal, List<Milestone> milestones) {
        double progressPercentage = calculateProgress(goal);

        List<GoalResponse.MilestoneResponse> milestoneResponses = milestones.stream()
                .map(m -> GoalResponse.MilestoneResponse.builder()
                        .id(m.getId())
                        .description(m.getDescription())
                        .targetValue(m.getTargetValue())
                        .achievedDate(m.getAchievedDate())
                        .seq(m.getSeq())
                        .build())
                .collect(Collectors.toList());

        return GoalResponse.builder()
                .id(goal.getId())
                .userId(goal.getUserId())
                .assessmentId(goal.getAssessmentId())
                .goalType(goal.getGoalType())
                .description(goal.getDescription())
                .targetValue(goal.getTargetValue())
                .startValue(goal.getStartValue())
                .currentValue(goal.getCurrentValue())
                .unit(goal.getUnit())
                .startDate(goal.getStartDate())
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus())
                .missedCheckIns(goal.getMissedCheckIns())
                .progressPercentage(progressPercentage)
                .milestones(milestoneResponses)
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    private BigDecimal resolveBaselineFromAssessment(Assessment assessment, Goal.GoalType goalType) {
        if (assessment == null) return BigDecimal.ZERO;
        switch (goalType) {
            case WEIGHT_LOSS:
            case WEIGHT_GAIN:
                // Read from encrypted metrics (authoritative source), fall back to plaintext column
                Double weight = readWeightFromEncryptedMetrics(assessment);
                if (weight == null) {
                    weight = assessment.getWeightLbs();
                }
                return weight != null ? BigDecimal.valueOf(weight) : BigDecimal.ZERO;
            case FLEXIBILITY:
            case ENDURANCE:
            case STRENGTH:
            default:
                return BigDecimal.ZERO;
        }
    }

    @SuppressWarnings("unchecked")
    private Double readWeightFromEncryptedMetrics(Assessment assessment) {
        String encrypted = assessment.getMetricsEncrypted();
        if (encrypted == null || encrypted.isBlank() || "PENDING_MIGRATION".equals(encrypted)) {
            return null;
        }
        try {
            Map<String, Object> metrics = objectMapper.readValue(encrypted, Map.class);
            Object val = metrics.get("weightLbs");
            if (val instanceof Number) return ((Number) val).doubleValue();
        } catch (Exception e) {
            log.warn("Failed to parse encrypted assessment metrics: {}", e.getMessage());
        }
        return null;
    }

    private double calculateProgress(Goal goal) {
        BigDecimal totalChange = goal.getTargetValue().subtract(goal.getStartValue());
        if (totalChange.compareTo(BigDecimal.ZERO) == 0) {
            return 100.0;
        }
        BigDecimal currentChange = goal.getCurrentValue().subtract(goal.getStartValue());
        return currentChange.divide(totalChange, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}
