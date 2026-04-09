package com.campusfit.masterdata.service;

import com.campusfit.masterdata.entity.Major;
import com.campusfit.masterdata.entity.MergeOperation;
import com.campusfit.masterdata.entity.School;
import com.campusfit.masterdata.repository.*;
import com.campusfit.study.repository.StudyPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DuplicateMergeServiceTest {

    @Mock
    private TermRepository termRepository;
    @Mock
    private SchoolRepository schoolRepository;
    @Mock
    private MajorRepository majorRepository;
    @Mock
    private AcademicClassRepository classRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private MergeOperationRepository mergeOperationRepository;
    @Mock
    private ChangeHistoryService changeHistoryService;
    @Mock
    private StudyPlanRepository studyPlanRepository;

    private DuplicateMergeService mergeService;

    @BeforeEach
    void setUp() {
        mergeService = new DuplicateMergeService(termRepository, schoolRepository,
                majorRepository, classRepository, courseRepository,
                mergeOperationRepository, changeHistoryService, studyPlanRepository);
    }

    @Test
    void merge_rePointsForeignKeys() {
        School source = School.builder().id(1L).code("SCH1").name("School 1")
                .effectiveFrom(LocalDate.now()).active(true).build();
        School target = School.builder().id(2L).code("SCH2").name("School 2")
                .effectiveFrom(LocalDate.now()).active(true).build();

        Major major = Major.builder().id(10L).schoolId(1L).code("MAJ1")
                .name("CS").effectiveFrom(LocalDate.now()).active(true).build();

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(source));
        when(schoolRepository.findById(2L)).thenReturn(Optional.of(target));
        when(majorRepository.findBySchoolId(1L)).thenReturn(List.of(major));
        when(majorRepository.save(any(Major.class))).thenReturn(major);
        when(schoolRepository.save(any(School.class))).thenReturn(source);
        when(mergeOperationRepository.save(any(MergeOperation.class)))
                .thenReturn(MergeOperation.builder().id(1L).build());

        mergeService.merge("SCHOOL", 1L, 2L, 42L);

        ArgumentCaptor<Major> majorCaptor = ArgumentCaptor.forClass(Major.class);
        verify(majorRepository).save(majorCaptor.capture());
        assertThat(majorCaptor.getValue().getSchoolId()).isEqualTo(2L);
    }

    @Test
    void merge_deletesSource() {
        School source = School.builder().id(1L).code("SCH1").name("School 1")
                .effectiveFrom(LocalDate.now()).active(true).build();
        School target = School.builder().id(2L).code("SCH2").name("School 2")
                .effectiveFrom(LocalDate.now()).active(true).build();

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(source));
        when(schoolRepository.findById(2L)).thenReturn(Optional.of(target));
        when(majorRepository.findBySchoolId(1L)).thenReturn(List.of());
        when(schoolRepository.save(any(School.class))).thenReturn(source);
        when(mergeOperationRepository.save(any(MergeOperation.class)))
                .thenReturn(MergeOperation.builder().id(1L).build());

        mergeService.merge("SCHOOL", 1L, 2L, 42L);

        ArgumentCaptor<School> schoolCaptor = ArgumentCaptor.forClass(School.class);
        verify(schoolRepository, atLeastOnce()).save(schoolCaptor.capture());

        // The source school should be soft-deleted (active = false)
        boolean sourceDeactivated = schoolCaptor.getAllValues().stream()
                .anyMatch(s -> s.getId().equals(1L) && !s.isActive());
        assertThat(sourceDeactivated).isTrue();
    }

    @Test
    void merge_createsAuditTrail() {
        School source = School.builder().id(1L).code("SCH1").name("School 1")
                .effectiveFrom(LocalDate.now()).active(true).build();
        School target = School.builder().id(2L).code("SCH2").name("School 2")
                .effectiveFrom(LocalDate.now()).active(true).build();

        Major major = Major.builder().id(10L).schoolId(1L).code("MAJ1")
                .name("CS").effectiveFrom(LocalDate.now()).active(true).build();

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(source));
        when(schoolRepository.findById(2L)).thenReturn(Optional.of(target));
        when(majorRepository.findBySchoolId(1L)).thenReturn(List.of(major));
        when(majorRepository.save(any())).thenReturn(major);
        when(schoolRepository.save(any())).thenReturn(source);
        when(mergeOperationRepository.save(any(MergeOperation.class)))
                .thenReturn(MergeOperation.builder().id(1L).build());

        mergeService.merge("SCHOOL", 1L, 2L, 42L);

        verify(changeHistoryService).logChange(eq("MAJOR"), eq(10L), eq("schoolId"),
                eq("1"), eq("2"), eq(42L));

        ArgumentCaptor<MergeOperation> opCaptor = ArgumentCaptor.forClass(MergeOperation.class);
        verify(mergeOperationRepository).save(opCaptor.capture());
        assertThat(opCaptor.getValue().getEntityType()).isEqualTo("SCHOOL");
        assertThat(opCaptor.getValue().getSourceId()).isEqualTo(1L);
        assertThat(opCaptor.getValue().getTargetId()).isEqualTo(2L);
    }

    @Test
    void merge_preservesSurvivingRecord() {
        School source = School.builder().id(1L).code("SCH1").name("School 1")
                .effectiveFrom(LocalDate.now()).active(true).build();
        School target = School.builder().id(2L).code("SCH2").name("School 2")
                .effectiveFrom(LocalDate.now()).active(true).build();

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(source));
        when(schoolRepository.findById(2L)).thenReturn(Optional.of(target));
        when(majorRepository.findBySchoolId(1L)).thenReturn(List.of());
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mergeOperationRepository.save(any(MergeOperation.class)))
                .thenReturn(MergeOperation.builder().id(1L).build());

        mergeService.merge("SCHOOL", 1L, 2L, 42L);

        // Target school should remain active and untouched
        assertThat(target.isActive()).isTrue();
        assertThat(target.getName()).isEqualTo("School 2");
    }
}
