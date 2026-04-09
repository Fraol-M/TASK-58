package com.campusfit.fitness.policy;

import com.campusfit.fitness.entity.Goal;
import com.campusfit.fitness.entity.GoalAdjustmentAudit;
import com.campusfit.fitness.repository.GoalAdjustmentAuditRepository;
import com.campusfit.fitness.repository.GoalRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class GoalRecalculationPolicy {

    private final GoalRepository goalRepository;
    private final GoalAdjustmentAuditRepository auditRepository;

    public GoalRecalculationPolicy(GoalRepository goalRepository,
                                   GoalAdjustmentAuditRepository auditRepository) {
        this.goalRepository = goalRepository;
        this.auditRepository = auditRepository;
    }

    /**
     * When 2 consecutive check-ins are missed:
     * - Extend targetDate by 20% of remaining duration
     * - Reduce weekly target by 10%
     * - Create GoalAdjustmentAudit record
     * - Reset missedCheckIns counter
     */
    @Transactional
    public void recalculate(Goal goal, Long adjustedBy) {
        BigDecimal previousTarget = goal.getTargetValue();
        LocalDate previousTargetDate = goal.getTargetDate();

        // Calculate remaining duration
        long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), goal.getTargetDate());
        if (remainingDays <= 0) {
            remainingDays = 7; // minimum fallback
        }

        // Extend target date by 20% of remaining duration
        long extensionDays = Math.max(1, (long) (remainingDays * 0.20));
        LocalDate newTargetDate = goal.getTargetDate().plusDays(extensionDays);

        // Reduce target by 10% (move target closer to current value)
        BigDecimal difference = goal.getTargetValue().subtract(goal.getCurrentValue());
        BigDecimal reduction = difference.multiply(BigDecimal.valueOf(0.10));
        BigDecimal newTarget = goal.getTargetValue().subtract(reduction).setScale(2, RoundingMode.HALF_UP);

        goal.setTargetDate(newTargetDate);
        goal.setTargetValue(newTarget);
        goal.setMissedCheckIns(0);
        goal.setStatus(Goal.GoalStatus.RECALCULATED);
        goalRepository.save(goal);

        // Create audit record
        GoalAdjustmentAudit audit = GoalAdjustmentAudit.builder()
                .goalId(goal.getId())
                .previousTarget(previousTarget)
                .newTarget(newTarget)
                .previousTargetDate(previousTargetDate)
                .newTargetDate(newTargetDate)
                .reason("Auto-recalculated due to 2 consecutive missed check-ins. " +
                        "Extended deadline by " + extensionDays + " days, reduced target by 10%.")
                .adjustedBy(adjustedBy)
                .build();
        auditRepository.save(audit);
    }
}
