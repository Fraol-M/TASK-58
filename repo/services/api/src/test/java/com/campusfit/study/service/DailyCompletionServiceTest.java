package com.campusfit.study.service;

import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import com.campusfit.study.dto.DailyCompletionRequest;
import com.campusfit.study.dto.DailyCompletionResponse;
import com.campusfit.study.entity.DailyCompletion;
import com.campusfit.study.entity.StudyPlan;
import com.campusfit.study.repository.DailyCompletionRepository;
import com.campusfit.study.repository.StudyPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyCompletionServiceTest {

    @Mock DailyCompletionRepository completionRepository;
    @Mock StudyPlanRepository studyPlanRepository;
    @Mock StreakService streakService;

    @InjectMocks DailyCompletionService dailyCompletionService;

    // ---- record ----

    @Test
    void record_completed_savesAndUpdatesStreak() {
        StudyPlan plan = plan(10L, 1L);
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));

        DailyCompletion saved = completion(1L, 10L, LocalDate.now(), true);
        when(completionRepository.save(any())).thenReturn(saved);

        DailyCompletionRequest req = DailyCompletionRequest.builder()
                .completedDate(LocalDate.now())
                .completed(true)
                .notes("Great session")
                .build();

        DailyCompletionResponse response = dailyCompletionService.record(10L, 1L, req);

        assertThat(response.isCompleted()).isTrue();
        assertThat(response.getPlanId()).isEqualTo(10L);
        verify(streakService, times(1)).updateStreak(1L, 10L, LocalDate.now());
    }

    @Test
    void record_notCompleted_doesNotUpdateStreak() {
        StudyPlan plan = plan(10L, 1L);
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));

        DailyCompletion saved = completion(1L, 10L, LocalDate.now(), false);
        when(completionRepository.save(any())).thenReturn(saved);

        DailyCompletionRequest req = DailyCompletionRequest.builder()
                .completedDate(LocalDate.now())
                .completed(false)
                .build();

        dailyCompletionService.record(10L, 1L, req);

        verify(streakService, never()).updateStreak(anyLong(), anyLong(), any());
    }

    @Test
    void record_foreignUser_throwsBusinessException() {
        StudyPlan plan = plan(10L, 1L);
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));

        DailyCompletionRequest req = DailyCompletionRequest.builder()
                .completedDate(LocalDate.now()).completed(true).build();

        assertThatThrownBy(() -> dailyCompletionService.record(10L, 99L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void record_planNotFound_throwsResourceNotFoundException() {
        when(studyPlanRepository.findById(999L)).thenReturn(Optional.empty());

        DailyCompletionRequest req = DailyCompletionRequest.builder()
                .completedDate(LocalDate.now()).completed(true).build();

        assertThatThrownBy(() -> dailyCompletionService.record(999L, 1L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void record_savesAllRequestFields() {
        StudyPlan plan = plan(10L, 1L);
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));

        DailyCompletion saved = DailyCompletion.builder()
                .id(5L).planId(10L).itemId(3L)
                .completedDate(LocalDate.of(2026, 4, 1))
                .completed(true).notes("Focused study")
                .build();
        when(completionRepository.save(any())).thenReturn(saved);

        DailyCompletionRequest req = DailyCompletionRequest.builder()
                .completedDate(LocalDate.of(2026, 4, 1))
                .completed(true).notes("Focused study").itemId(3L)
                .build();

        DailyCompletionResponse response = dailyCompletionService.record(10L, 1L, req);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getItemId()).isEqualTo(3L);
        assertThat(response.getNotes()).isEqualTo("Focused study");
        assertThat(response.getCompletedDate()).isEqualTo(LocalDate.of(2026, 4, 1));
    }

    // ---- getByPlanId ----

    @Test
    void getByPlanId_ownerAccess_returnsList() {
        StudyPlan plan = plan(10L, 1L);
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(completionRepository.findByPlanId(10L)).thenReturn(List.of(
                completion(1L, 10L, LocalDate.now(), true),
                completion(2L, 10L, LocalDate.now().minusDays(1), false)
        ));

        List<DailyCompletionResponse> result = dailyCompletionService.getByPlanId(10L, 1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).isCompleted()).isTrue();
        assertThat(result.get(1).isCompleted()).isFalse();
    }

    @Test
    void getByPlanId_emptyPlan_returnsEmptyList() {
        StudyPlan plan = plan(10L, 1L);
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(completionRepository.findByPlanId(10L)).thenReturn(List.of());

        List<DailyCompletionResponse> result = dailyCompletionService.getByPlanId(10L, 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getByPlanId_foreignUser_throwsBusinessException() {
        StudyPlan plan = plan(10L, 1L);
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> dailyCompletionService.getByPlanId(10L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("permission");
    }

    // ---- helpers ----

    private StudyPlan plan(Long planId, Long userId) {
        return StudyPlan.builder().id(planId).userId(userId).title("Plan")
                .status(StudyPlan.PlanStatus.ACTIVE).build();
    }

    private DailyCompletion completion(Long id, Long planId, LocalDate date, boolean completed) {
        return DailyCompletion.builder()
                .id(id).planId(planId).completedDate(date).completed(completed).build();
    }
}
