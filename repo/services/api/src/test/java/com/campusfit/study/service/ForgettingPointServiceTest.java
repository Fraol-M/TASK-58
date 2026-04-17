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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgettingPointServiceTest {

    @Mock ForgettingPointRepository forgettingPointRepository;
    @Mock StudyPlanRepository studyPlanRepository;

    @InjectMocks ForgettingPointService forgettingPointService;

    // ---- create ----

    @Test
    void create_ownerPlan_persistsAndReturns() {
        StudyPlan plan = plan(10L, 1L);
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(forgettingPointRepository.save(any())).thenAnswer(inv -> {
            ForgettingPoint fp = inv.getArgument(0);
            fp.setId(99L);
            return fp;
        });

        ForgettingPointRequest req = ForgettingPointRequest.builder().topic("Calculus").build();
        ForgettingPointResponse response = forgettingPointService.create(10L, 1L, req);

        assertThat(response.getTopic()).isEqualTo("Calculus");
        assertThat(response.getNextReviewDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(response.getIntervalDays()).isEqualTo(1);
        assertThat(response.getRepetitions()).isEqualTo(0);
        assertThat(response.getEaseFactor()).isEqualByComparingTo("2.50");
    }

    @Test
    void create_foreignUser_throwsBusinessException() {
        StudyPlan plan = plan(10L, 1L);
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));

        ForgettingPointRequest req = ForgettingPointRequest.builder().topic("Topic").build();

        assertThatThrownBy(() -> forgettingPointService.create(10L, 99L, req))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void create_planNotFound_throwsResourceNotFoundException() {
        when(studyPlanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forgettingPointService.create(999L, 1L,
                ForgettingPointRequest.builder().topic("X").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- getByPlanId ----

    @Test
    void getByPlanId_ownerAccess_returnsList() {
        StudyPlan plan = plan(10L, 1L);
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(forgettingPointRepository.findByPlanId(10L)).thenReturn(List.of(fp(1L, 10L, "Topic A")));

        List<ForgettingPointResponse> result = forgettingPointService.getByPlanId(10L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTopic()).isEqualTo("Topic A");
    }

    @Test
    void getByPlanId_foreignUser_throwsBusinessException() {
        StudyPlan plan = plan(10L, 1L);
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> forgettingPointService.getByPlanId(10L, 99L))
                .isInstanceOf(BusinessException.class);
    }

    // ---- review: SM-2 algorithm ----

    @Test
    void review_quality5_firstRepetition_intervalStays1_efIncreases() {
        StudyPlan plan = plan(10L, 1L);
        ForgettingPoint point = fp(1L, 10L, "Topic");
        // Initial state: repetitions=0, interval=1, EF=2.50
        when(forgettingPointRepository.findById(1L)).thenReturn(Optional.of(point));
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(forgettingPointRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ForgettingPointResponse response = forgettingPointService.review(1L, 1L,
                ReviewRequest.builder().quality(5).build());

        // repetitions==0 → interval=1, nextReview=today+1
        assertThat(response.getIntervalDays()).isEqualTo(1);
        assertThat(response.getNextReviewDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(response.getRepetitions()).isEqualTo(1);
        // quality=5: diff=0, adj=0.1, EF=2.50+0.10=2.60
        assertThat(response.getEaseFactor()).isEqualByComparingTo("2.60");
    }

    @Test
    void review_quality4_secondRepetition_intervalBecomes6() {
        StudyPlan plan = plan(10L, 1L);
        ForgettingPoint point = fp(1L, 10L, "Topic");
        point.setRepetitions(1);
        point.setIntervalDays(1);
        when(forgettingPointRepository.findById(1L)).thenReturn(Optional.of(point));
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(forgettingPointRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ForgettingPointResponse response = forgettingPointService.review(1L, 1L,
                ReviewRequest.builder().quality(4).build());

        // repetitions==1 → interval=6, nextReview=today+6
        assertThat(response.getIntervalDays()).isEqualTo(6);
        assertThat(response.getNextReviewDate()).isEqualTo(LocalDate.now().plusDays(6));
        assertThat(response.getRepetitions()).isEqualTo(2);
        // quality=4: diff=1, adj=0.1-1*(0.08+0.02)=0, EF=2.50
        assertThat(response.getEaseFactor()).isEqualByComparingTo("2.50");
    }

    @Test
    void review_quality4_thirdRepetition_intervalMultipliedByEf() {
        StudyPlan plan = plan(10L, 1L);
        ForgettingPoint point = fp(1L, 10L, "Topic");
        point.setRepetitions(2);
        point.setIntervalDays(6);
        point.setEaseFactor(new BigDecimal("2.50"));
        when(forgettingPointRepository.findById(1L)).thenReturn(Optional.of(point));
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(forgettingPointRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ForgettingPointResponse response = forgettingPointService.review(1L, 1L,
                ReviewRequest.builder().quality(4).build());

        // repetitions>=2 → interval = ceil(6 * 2.50) = ceil(15.0) = 15
        assertThat(response.getIntervalDays()).isEqualTo(15);
        assertThat(response.getNextReviewDate()).isEqualTo(LocalDate.now().plusDays(15));
    }

    @Test
    void review_qualityBelow3_resetsIntervalAndRepetitions() {
        StudyPlan plan = plan(10L, 1L);
        ForgettingPoint point = fp(1L, 10L, "Topic");
        point.setRepetitions(3);
        point.setIntervalDays(15);
        when(forgettingPointRepository.findById(1L)).thenReturn(Optional.of(point));
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(forgettingPointRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ForgettingPointResponse response = forgettingPointService.review(1L, 1L,
                ReviewRequest.builder().quality(1).build());

        // Failed recall: interval=1, repetitions=0, nextReview=today+1
        assertThat(response.getIntervalDays()).isEqualTo(1);
        assertThat(response.getRepetitions()).isEqualTo(0);
        assertThat(response.getNextReviewDate()).isEqualTo(LocalDate.now().plusDays(1));
        // quality=1: diff=4, adj=0.1-4*(0.08+0.08)=0.1-0.64=-0.54, EF=2.50-0.54=1.96
        assertThat(response.getEaseFactor()).isEqualByComparingTo("1.96");
    }

    @Test
    void review_efCannotDropBelow1_3() {
        StudyPlan plan = plan(10L, 1L);
        ForgettingPoint point = fp(1L, 10L, "Topic");
        point.setEaseFactor(new BigDecimal("1.40"));
        when(forgettingPointRepository.findById(1L)).thenReturn(Optional.of(point));
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));
        when(forgettingPointRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ForgettingPointResponse response = forgettingPointService.review(1L, 1L,
                ReviewRequest.builder().quality(0).build());

        // quality=0: diff=5, adj=0.1-5*(0.08+0.10)=-0.80; 1.40-0.80=0.60 → clamped to 1.30
        assertThat(response.getEaseFactor()).isEqualByComparingTo("1.30");
    }

    @Test
    void review_foreignUser_throwsBusinessException() {
        ForgettingPoint point = fp(1L, 10L, "Topic");
        StudyPlan plan = plan(10L, 1L);
        when(forgettingPointRepository.findById(1L)).thenReturn(Optional.of(point));
        when(studyPlanRepository.findById(10L)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> forgettingPointService.review(1L, 99L,
                ReviewRequest.builder().quality(4).build()))
                .isInstanceOf(BusinessException.class);
    }

    // ---- helpers ----

    private StudyPlan plan(Long planId, Long userId) {
        return StudyPlan.builder()
                .id(planId).userId(userId).title("Plan")
                .status(StudyPlan.PlanStatus.ACTIVE)
                .build();
    }

    private ForgettingPoint fp(Long id, Long planId, String topic) {
        ForgettingPoint fp = new ForgettingPoint();
        fp.setId(id);
        fp.setPlanId(planId);
        fp.setTopic(topic);
        fp.setNextReviewDate(LocalDate.now().plusDays(1));
        fp.setEaseFactor(new BigDecimal("2.50"));
        fp.setIntervalDays(1);
        fp.setRepetitions(0);
        return fp;
    }
}
