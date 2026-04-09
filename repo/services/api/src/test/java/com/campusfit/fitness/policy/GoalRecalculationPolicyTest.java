package com.campusfit.fitness.policy;

import com.campusfit.fitness.entity.Goal;
import com.campusfit.fitness.entity.GoalAdjustmentAudit;
import com.campusfit.fitness.repository.GoalAdjustmentAuditRepository;
import com.campusfit.fitness.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalRecalculationPolicyTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalAdjustmentAuditRepository auditRepository;

    private GoalRecalculationPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new GoalRecalculationPolicy(goalRepository, auditRepository);
    }

    @Test
    void recalculate_extendsTargetDate() {
        LocalDate targetDate = LocalDate.now().plusDays(50);
        Goal goal = Goal.builder()
                .id(1L)
                .userId(100L)
                .targetValue(new BigDecimal("100.00"))
                .currentValue(new BigDecimal("40.00"))
                .startValue(BigDecimal.ZERO)
                .startDate(LocalDate.now().minusDays(30))
                .targetDate(targetDate)
                .status(Goal.GoalStatus.ACTIVE)
                .missedCheckIns(2)
                .build();

        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(auditRepository.save(any(GoalAdjustmentAudit.class))).thenReturn(GoalAdjustmentAudit.builder().build());

        policy.recalculate(goal, 100L);

        // 20% of 50 remaining days = 10 days extension
        assertThat(goal.getTargetDate()).isEqualTo(targetDate.plusDays(10));
    }

    @Test
    void recalculate_reducesWeeklyTarget() {
        Goal goal = Goal.builder()
                .id(1L)
                .userId(100L)
                .targetValue(new BigDecimal("100.00"))
                .currentValue(new BigDecimal("40.00"))
                .startValue(BigDecimal.ZERO)
                .startDate(LocalDate.now().minusDays(30))
                .targetDate(LocalDate.now().plusDays(50))
                .status(Goal.GoalStatus.ACTIVE)
                .missedCheckIns(2)
                .build();

        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(auditRepository.save(any(GoalAdjustmentAudit.class))).thenReturn(GoalAdjustmentAudit.builder().build());

        policy.recalculate(goal, 100L);

        // difference = 100 - 40 = 60; reduction = 60 * 0.10 = 6.00; new target = 100 - 6 = 94.00
        assertThat(goal.getTargetValue()).isEqualByComparingTo(new BigDecimal("94.00"));
    }

    @Test
    void recalculate_createsAuditRecord() {
        Goal goal = Goal.builder()
                .id(1L)
                .userId(100L)
                .targetValue(new BigDecimal("100.00"))
                .currentValue(new BigDecimal("40.00"))
                .startValue(BigDecimal.ZERO)
                .startDate(LocalDate.now().minusDays(30))
                .targetDate(LocalDate.now().plusDays(50))
                .status(Goal.GoalStatus.ACTIVE)
                .missedCheckIns(2)
                .build();

        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(auditRepository.save(any(GoalAdjustmentAudit.class))).thenReturn(GoalAdjustmentAudit.builder().build());

        policy.recalculate(goal, 100L);

        ArgumentCaptor<GoalAdjustmentAudit> auditCaptor = ArgumentCaptor.forClass(GoalAdjustmentAudit.class);
        verify(auditRepository).save(auditCaptor.capture());

        GoalAdjustmentAudit audit = auditCaptor.getValue();
        assertThat(audit.getGoalId()).isEqualTo(1L);
        assertThat(audit.getPreviousTarget()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(audit.getNewTarget()).isEqualByComparingTo(new BigDecimal("94.00"));
        assertThat(audit.getAdjustedBy()).isEqualTo(100L);
        assertThat(audit.getReason()).contains("Auto-recalculated");
    }

    @Test
    void recalculate_resetsMissedCounter() {
        Goal goal = Goal.builder()
                .id(1L)
                .userId(100L)
                .targetValue(new BigDecimal("100.00"))
                .currentValue(new BigDecimal("40.00"))
                .startValue(BigDecimal.ZERO)
                .startDate(LocalDate.now().minusDays(30))
                .targetDate(LocalDate.now().plusDays(50))
                .status(Goal.GoalStatus.ACTIVE)
                .missedCheckIns(3)
                .build();

        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(auditRepository.save(any(GoalAdjustmentAudit.class))).thenReturn(GoalAdjustmentAudit.builder().build());

        policy.recalculate(goal, 100L);

        assertThat(goal.getMissedCheckIns()).isEqualTo(0);
        assertThat(goal.getStatus()).isEqualTo(Goal.GoalStatus.RECALCULATED);
    }
}
