package com.campusfit.fitness.service;

import com.campusfit.fitness.dto.CheckInRequest;
import com.campusfit.fitness.dto.CheckInResponse;
import com.campusfit.fitness.entity.CheckIn;
import com.campusfit.fitness.entity.Goal;
import com.campusfit.fitness.policy.GoalRecalculationPolicy;
import com.campusfit.fitness.repository.CheckInRepository;
import com.campusfit.fitness.repository.GoalRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final GoalRepository goalRepository;
    private final GoalRecalculationPolicy recalculationPolicy;

    public CheckInService(CheckInRepository checkInRepository,
                          GoalRepository goalRepository,
                          GoalRecalculationPolicy recalculationPolicy) {
        this.checkInRepository = checkInRepository;
        this.goalRepository = goalRepository;
        this.recalculationPolicy = recalculationPolicy;
    }

    @Transactional
    public CheckInResponse create(Long goalId, Long userId, CheckInRequest request) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));

        if (!goal.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to check in on this goal");
        }

        if (goal.getStatus() == Goal.GoalStatus.ABANDONED) {
            throw new BusinessException("Cannot check in on an abandoned goal");
        }

        // Determine the next week number based on elapsed time from goal start
        List<CheckIn> existingCheckIns = checkInRepository.findByGoalIdOrderByWeekNumberDesc(goalId);
        long weeksElapsed = ChronoUnit.WEEKS.between(goal.getStartDate().atStartOfDay(), LocalDateTime.now());
        int currentWeek = Math.max(1, (int) weeksElapsed + 1);
        int lastRecordedWeek = existingCheckIns.isEmpty() ? 0 : existingCheckIns.get(0).getWeekNumber();
        int nextWeekNumber = Math.max(currentWeek, lastRecordedWeek + 1);

        // Calculate missed weeks based on gap since last check-in
        int missedWeeks = nextWeekNumber - lastRecordedWeek - 1;

        CheckIn checkIn = CheckIn.builder()
                .goalId(goalId)
                .userId(userId)
                .weekNumber(nextWeekNumber)
                .value(request.getValue())
                .valueEncrypted(request.getValue().toPlainString())
                .notes(request.getNotes())
                .build();

        CheckIn saved = checkInRepository.save(checkIn);

        // Update goal's current value
        goal.setCurrentValue(request.getValue());

        // Track consecutive missed check-ins
        if (missedWeeks > 0) {
            goal.setMissedCheckIns(goal.getMissedCheckIns() + missedWeeks);
        } else {
            // Successful check-in resets missed counter
            goal.setMissedCheckIns(0);
        }

        // Check if consecutive missed check-ins >= 2
        if (goal.getMissedCheckIns() >= 2) {
            recalculationPolicy.recalculate(goal, userId);
        } else {
            goalRepository.save(goal);
        }

        // Check if goal is achieved using goal-type-aware comparison
        boolean achieved;
        if (goal.getGoalType() == Goal.GoalType.WEIGHT_LOSS) {
            // For weight loss, achievement means current value <= target (went down to target)
            achieved = request.getValue().compareTo(goal.getTargetValue()) <= 0;
        } else {
            // For weight gain, endurance, strength, etc., achievement means current value >= target
            achieved = request.getValue().compareTo(goal.getTargetValue()) >= 0;
        }
        if (achieved) {
            goal.setStatus(Goal.GoalStatus.ACHIEVED);
            goalRepository.save(goal);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CheckInResponse> getByGoalId(Long goalId, Long userId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));

        if (!goal.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to view check-ins for this goal");
        }

        return checkInRepository.findByGoalIdOrderByWeekNumberDesc(goalId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CheckInResponse toResponse(CheckIn c) {
        return CheckInResponse.builder()
                .id(c.getId())
                .goalId(c.getGoalId())
                .userId(c.getUserId())
                .weekNumber(c.getWeekNumber())
                .value(c.getValue())
                .notes(c.getNotes())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
