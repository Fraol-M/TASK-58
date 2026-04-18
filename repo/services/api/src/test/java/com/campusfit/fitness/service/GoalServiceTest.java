package com.campusfit.fitness.service;

import com.campusfit.fitness.dto.GoalRequest;
import com.campusfit.fitness.dto.GoalResponse;
import com.campusfit.fitness.entity.Assessment;
import com.campusfit.fitness.entity.Goal;
import com.campusfit.fitness.entity.Milestone;
import com.campusfit.fitness.repository.AssessmentRepository;
import com.campusfit.fitness.repository.GoalRepository;
import com.campusfit.fitness.repository.MilestoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    private GoalService goalService;

    @BeforeEach
    void setUp() {
        goalService = new GoalService(goalRepository, milestoneRepository, assessmentRepository);
    }

    @Test
    void createGoal_generatesMilestones() {
        GoalRequest request = GoalRequest.builder()
                .goalType(Goal.GoalType.WEIGHT_LOSS)
                .description("Lose weight for summer")
                .targetValue(new BigDecimal("70.00"))
                .unit("kg")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusWeeks(12))
                .build();

        // No assessment exists — baseline defaults to zero
        when(assessmentRepository.findTopByUserIdOrderByAssessmentDateDescIdDesc(100L))
                .thenReturn(Optional.empty());

        Goal savedGoal = Goal.builder()
                .id(1L)
                .userId(100L)
                .goalType(Goal.GoalType.WEIGHT_LOSS)
                .description("Lose weight for summer")
                .targetValue(new BigDecimal("70.00"))
                .startValue(BigDecimal.ZERO)
                .currentValue(BigDecimal.ZERO)
                .unit("kg")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusWeeks(12))
                .status(Goal.GoalStatus.ACTIVE)
                .missedCheckIns(0)
                .build();

        when(goalRepository.save(any(Goal.class))).thenReturn(savedGoal);
        when(milestoneRepository.findByGoalIdOrderBySeq(1L)).thenReturn(List.of());

        goalService.create(100L, request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Milestone>> milestonesCaptor = ArgumentCaptor.forClass(List.class);
        verify(milestoneRepository).saveAll(milestonesCaptor.capture());

        List<Milestone> milestones = milestonesCaptor.getValue();
        assertThat(milestones).hasSize(4);
        assertThat(milestones.get(0).getSeq()).isEqualTo(1);
        assertThat(milestones.get(3).getSeq()).isEqualTo(4);
        assertThat(milestones.get(3).getTargetValue()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    void createGoal_noAssessment_setsZeroBaseline() {
        GoalRequest request = GoalRequest.builder()
                .goalType(Goal.GoalType.ENDURANCE)
                .description("Run a marathon")
                .targetValue(new BigDecimal("42.20"))
                .unit("km")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusWeeks(12))
                .build();

        when(assessmentRepository.findTopByUserIdOrderByAssessmentDateDescIdDesc(100L))
                .thenReturn(Optional.empty());

        Goal savedGoal = Goal.builder()
                .id(2L)
                .userId(100L)
                .goalType(Goal.GoalType.ENDURANCE)
                .targetValue(new BigDecimal("42.20"))
                .startValue(BigDecimal.ZERO)
                .currentValue(BigDecimal.ZERO)
                .unit("km")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusWeeks(12))
                .status(Goal.GoalStatus.ACTIVE)
                .missedCheckIns(0)
                .build();

        when(goalRepository.save(any(Goal.class))).thenReturn(savedGoal);
        when(milestoneRepository.findByGoalIdOrderBySeq(2L)).thenReturn(List.of());

        goalService.create(100L, request);

        ArgumentCaptor<Goal> goalCaptor = ArgumentCaptor.forClass(Goal.class);
        verify(goalRepository).save(goalCaptor.capture());

        Goal capturedGoal = goalCaptor.getValue();
        assertThat(capturedGoal.getStartValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(capturedGoal.getCurrentValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(capturedGoal.getStatus()).isEqualTo(Goal.GoalStatus.ACTIVE);
    }

    @Test
    void createWeightLossGoal_withAssessment_usesAssessmentWeight() {
        Assessment assessment = Assessment.builder()
                .id(10L)
                .userId(100L)
                .weightLbs(200.0)
                .metricsEncrypted("{\"weightLbs\":200.0}")
                .assessmentDate(LocalDate.now().minusDays(7))
                .build();

        when(assessmentRepository.findTopByUserIdOrderByAssessmentDateDescIdDesc(100L))
                .thenReturn(Optional.of(assessment));

        GoalRequest request = GoalRequest.builder()
                .goalType(Goal.GoalType.WEIGHT_LOSS)
                .description("Lose 20 lbs")
                .targetValue(new BigDecimal("180.00"))
                .unit("lbs")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusWeeks(16))
                .build();

        Goal savedGoal = Goal.builder()
                .id(3L)
                .userId(100L)
                .goalType(Goal.GoalType.WEIGHT_LOSS)
                .targetValue(new BigDecimal("180.00"))
                .startValue(new BigDecimal("200.0"))
                .currentValue(new BigDecimal("200.0"))
                .unit("lbs")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusWeeks(16))
                .status(Goal.GoalStatus.ACTIVE)
                .missedCheckIns(0)
                .build();

        when(goalRepository.save(any(Goal.class))).thenReturn(savedGoal);
        when(milestoneRepository.findByGoalIdOrderBySeq(3L)).thenReturn(List.of());

        goalService.create(100L, request);

        ArgumentCaptor<Goal> goalCaptor = ArgumentCaptor.forClass(Goal.class);
        verify(goalRepository).save(goalCaptor.capture());

        Goal capturedGoal = goalCaptor.getValue();
        // Baseline should come from assessment weight, not zero
        assertThat(capturedGoal.getStartValue()).isEqualByComparingTo(new BigDecimal("200.0"));
        assertThat(capturedGoal.getCurrentValue()).isEqualByComparingTo(new BigDecimal("200.0"));
        assertThat(capturedGoal.getAssessmentId()).isEqualTo(10L);
    }
}
