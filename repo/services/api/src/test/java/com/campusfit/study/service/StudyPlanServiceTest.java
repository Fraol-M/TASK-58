package com.campusfit.study.service;

import com.campusfit.masterdata.repository.*;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import com.campusfit.study.dto.StudyPlanRequest;
import com.campusfit.study.dto.StudyPlanResponse;
import com.campusfit.study.entity.StudyPlan;
import com.campusfit.study.repository.StudyPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyPlanServiceTest {

    @Mock StudyPlanRepository studyPlanRepository;
    @Mock TermRepository termRepository;
    @Mock SchoolRepository schoolRepository;
    @Mock MajorRepository majorRepository;
    @Mock AcademicClassRepository classRepository;
    @Mock CourseRepository courseRepository;

    @InjectMocks StudyPlanService studyPlanService;

    // ---- create ----

    @Test
    void create_noHierarchyRefs_savesAndReturns() {
        StudyPlanRequest req = StudyPlanRequest.builder().title("My Plan").build();
        StudyPlan saved = planEntity(1L, 10L, "My Plan");
        when(studyPlanRepository.save(any())).thenReturn(saved);

        StudyPlanResponse response = studyPlanService.create(10L, req);

        assertThat(response.getTitle()).isEqualTo("My Plan");
        assertThat(response.getId()).isEqualTo(1L);
        verify(studyPlanRepository).save(any(StudyPlan.class));
    }

    @Test
    void create_setsAllHierarchyFields() {
        StudyPlanRequest req = StudyPlanRequest.builder()
                .title("Plan")
                .termId(1L).schoolId(2L).majorId(3L).classId(4L).courseId(5L)
                .build();

        when(termRepository.existsById(1L)).thenReturn(true);
        when(schoolRepository.existsById(2L)).thenReturn(true);
        when(majorRepository.existsById(3L)).thenReturn(true);
        when(classRepository.existsById(4L)).thenReturn(true);
        when(courseRepository.existsById(5L)).thenReturn(true);

        StudyPlan saved = StudyPlan.builder()
                .id(1L).userId(10L).title("Plan")
                .termId(1L).schoolId(2L).majorId(3L).classId(4L).courseId(5L)
                .status(StudyPlan.PlanStatus.ACTIVE)
                .build();
        when(studyPlanRepository.save(any())).thenReturn(saved);

        StudyPlanResponse response = studyPlanService.create(10L, req);

        assertThat(response.getTermId()).isEqualTo(1L);
        assertThat(response.getSchoolId()).isEqualTo(2L);
        assertThat(response.getMajorId()).isEqualTo(3L);
        assertThat(response.getClassId()).isEqualTo(4L);
        assertThat(response.getCourseId()).isEqualTo(5L);
    }

    // ---- hierarchy validation ----

    @Test
    void create_nonExistentTermId_throwsBusinessException() {
        StudyPlanRequest req = StudyPlanRequest.builder().title("Plan").termId(99L).build();
        when(termRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> studyPlanService.create(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Term with id 99");
    }

    @Test
    void create_nonExistentSchoolId_throwsBusinessException() {
        StudyPlanRequest req = StudyPlanRequest.builder().title("Plan").schoolId(99L).build();
        when(schoolRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> studyPlanService.create(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("School with id 99");
    }

    @Test
    void create_nonExistentMajorId_throwsBusinessException() {
        StudyPlanRequest req = StudyPlanRequest.builder().title("Plan").majorId(99L).build();
        when(majorRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> studyPlanService.create(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Major with id 99");
    }

    @Test
    void create_nonExistentClassId_throwsBusinessException() {
        StudyPlanRequest req = StudyPlanRequest.builder().title("Plan").classId(99L).build();
        when(classRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> studyPlanService.create(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Class with id 99");
    }

    @Test
    void create_nonExistentCourseId_throwsBusinessException() {
        StudyPlanRequest req = StudyPlanRequest.builder().title("Plan").courseId(99L).build();
        when(courseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> studyPlanService.create(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Course with id 99");
    }

    // ---- getById ----

    @Test
    void getById_ownerAccess_returnsResponse() {
        StudyPlan plan = planEntity(5L, 10L, "My Plan");
        when(studyPlanRepository.findById(5L)).thenReturn(Optional.of(plan));

        StudyPlanResponse response = studyPlanService.getById(5L, 10L);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getTitle()).isEqualTo("My Plan");
    }

    @Test
    void getById_foreignUser_throwsBusinessException() {
        StudyPlan plan = planEntity(5L, 10L, "My Plan");
        when(studyPlanRepository.findById(5L)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> studyPlanService.getById(5L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(studyPlanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyPlanService.getById(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- getAllForUser (pageable) ----

    @Test
    void getAllForUser_pageable_returnsPage() {
        StudyPlan plan = planEntity(1L, 10L, "Plan A");
        Pageable pageable = PageRequest.of(0, 25);
        when(studyPlanRepository.findByUserId(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(plan)));

        var page = studyPlanService.getAllForUser(10L, pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Plan A");
    }

    // ---- update ----

    @Test
    void update_ownerUpdatesTitle() {
        StudyPlan existing = planEntity(1L, 10L, "Old Title");
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studyPlanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StudyPlanRequest req = StudyPlanRequest.builder().title("New Title").build();
        StudyPlanResponse response = studyPlanService.update(1L, 10L, req);

        assertThat(response.getTitle()).isEqualTo("New Title");
    }

    @Test
    void update_foreignUser_throwsBusinessException() {
        StudyPlan existing = planEntity(1L, 10L, "Old Title");
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(existing));

        StudyPlanRequest req = StudyPlanRequest.builder().title("New Title").build();

        assertThatThrownBy(() -> studyPlanService.update(1L, 99L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("permission");
    }

    // ---- delete ----

    @Test
    void delete_ownerArchivesPlan() {
        StudyPlan existing = planEntity(1L, 10L, "Plan");
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(existing));

        ArgumentCaptor<StudyPlan> captor = ArgumentCaptor.forClass(StudyPlan.class);
        when(studyPlanRepository.save(captor.capture())).thenReturn(existing);

        studyPlanService.delete(1L, 10L);

        assertThat(captor.getValue().getStatus()).isEqualTo(StudyPlan.PlanStatus.ARCHIVED);
    }

    @Test
    void delete_foreignUser_throwsBusinessException() {
        StudyPlan existing = planEntity(1L, 10L, "Plan");
        when(studyPlanRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> studyPlanService.delete(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("permission");
    }

    // ---- helpers ----

    private StudyPlan planEntity(Long id, Long userId, String title) {
        return StudyPlan.builder()
                .id(id).userId(userId).title(title)
                .status(StudyPlan.PlanStatus.ACTIVE)
                .build();
    }
}
