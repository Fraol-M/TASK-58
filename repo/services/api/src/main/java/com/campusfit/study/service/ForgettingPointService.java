package com.campusfit.study.service;

import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import com.campusfit.study.dto.ForgettingPointRequest;
import com.campusfit.study.dto.ForgettingPointResponse;
import com.campusfit.study.dto.ReviewRequest;
import com.campusfit.study.entity.ForgettingPoint;
import com.campusfit.study.entity.StudyPlan;
import com.campusfit.study.repository.ForgettingPointRepository;
import com.campusfit.study.repository.StudyPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForgettingPointService {

    private final ForgettingPointRepository forgettingPointRepository;
    private final StudyPlanRepository studyPlanRepository;

    public ForgettingPointService(ForgettingPointRepository forgettingPointRepository,
                                  StudyPlanRepository studyPlanRepository) {
        this.forgettingPointRepository = forgettingPointRepository;
        this.studyPlanRepository = studyPlanRepository;
    }

    @Transactional
    public ForgettingPointResponse create(Long planId, Long userId, ForgettingPointRequest request) {
        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyPlan", planId));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to add forgetting points to this plan");
        }

        ForgettingPoint point = ForgettingPoint.builder()
                .planId(planId)
                .topic(request.getTopic())
                .description(request.getDescription())
                .nextReviewDate(LocalDate.now().plusDays(1))
                .easeFactor(new BigDecimal("2.50"))
                .intervalDays(1)
                .repetitions(0)
                .build();

        ForgettingPoint saved = forgettingPointRepository.save(point);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ForgettingPointResponse> getByPlanId(Long planId, Long userId) {
        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyPlan", planId));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to view forgetting points for this plan");
        }

        return forgettingPointRepository.findByPlanId(planId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * SM-2 algorithm for spaced repetition.
     * Quality: 0-5 (0=total blackout, 5=perfect recall)
     *
     * If quality >= 3 (successful recall):
     *   - If repetitions == 0: interval = 1
     *   - If repetitions == 1: interval = 6
     *   - Otherwise: interval = interval * easeFactor
     *   - Increment repetitions
     * Else (failed recall):
     *   - Reset interval to 1
     *   - Reset repetitions to 0
     *
     * Adjust ease factor:
     *   EF' = EF + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
     *   EF minimum = 1.3
     */
    @Transactional
    public ForgettingPointResponse review(Long id, Long userId, ReviewRequest request) {
        ForgettingPoint point = forgettingPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ForgettingPoint", id));

        StudyPlan plan = studyPlanRepository.findById(point.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("StudyPlan", point.getPlanId()));
        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to review this forgetting point");
        }

        int quality = request.getQuality();

        if (quality >= 3) {
            // Successful recall
            if (point.getRepetitions() == 0) {
                point.setIntervalDays(1);
            } else if (point.getRepetitions() == 1) {
                point.setIntervalDays(6);
            } else {
                int newInterval = BigDecimal.valueOf(point.getIntervalDays())
                        .multiply(point.getEaseFactor())
                        .setScale(0, RoundingMode.CEILING)
                        .intValue();
                point.setIntervalDays(newInterval);
            }
            point.setRepetitions(point.getRepetitions() + 1);
        } else {
            // Failed recall
            point.setIntervalDays(1);
            point.setRepetitions(0);
        }

        // Adjust ease factor: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        int diff = 5 - quality;
        BigDecimal adjustment = BigDecimal.valueOf(0.1)
                .subtract(BigDecimal.valueOf(diff)
                        .multiply(BigDecimal.valueOf(0.08)
                                .add(BigDecimal.valueOf(diff).multiply(BigDecimal.valueOf(0.02)))));

        BigDecimal newEf = point.getEaseFactor().add(adjustment).setScale(2, RoundingMode.HALF_UP);
        if (newEf.compareTo(BigDecimal.valueOf(1.3)) < 0) {
            newEf = BigDecimal.valueOf(1.3).setScale(2, RoundingMode.HALF_UP);
        }
        point.setEaseFactor(newEf);

        // Set next review date
        point.setNextReviewDate(LocalDate.now().plusDays(point.getIntervalDays()));

        ForgettingPoint saved = forgettingPointRepository.save(point);
        return toResponse(saved);
    }

    private ForgettingPointResponse toResponse(ForgettingPoint fp) {
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
