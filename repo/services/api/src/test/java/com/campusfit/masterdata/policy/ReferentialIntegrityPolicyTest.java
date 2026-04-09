package com.campusfit.masterdata.policy;

import com.campusfit.masterdata.entity.Major;
import com.campusfit.masterdata.repository.AcademicClassRepository;
import com.campusfit.masterdata.repository.CourseRepository;
import com.campusfit.masterdata.repository.MajorRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.study.entity.StudyPlan;
import com.campusfit.study.repository.StudyPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferentialIntegrityPolicyTest {

    @Mock
    private MajorRepository majorRepository;
    @Mock
    private AcademicClassRepository classRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private StudyPlanRepository studyPlanRepository;

    private ReferentialIntegrityPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new ReferentialIntegrityPolicy(majorRepository, classRepository,
                courseRepository, studyPlanRepository);
    }

    @Test
    void canDelete_returnsFalse_whenReferencedByStudyPlan() {
        // School is referenced by majors -> cannot delete
        when(majorRepository.findBySchoolId(1L)).thenReturn(List.of(
                Major.builder().id(10L).schoolId(1L).name("CS").code("CS").build()
        ));

        assertThatThrownBy(() -> policy.checkBeforeDeleteSchool(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete school");
    }

    @Test
    void canDelete_returnsTrue_whenNotReferenced() {
        when(majorRepository.findBySchoolId(1L)).thenReturn(List.of());

        assertThatCode(() -> policy.checkBeforeDeleteSchool(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void delete_throwsException_whenReferenced() {
        when(classRepository.findByMajorId(5L)).thenReturn(List.of(
                com.campusfit.masterdata.entity.AcademicClass.builder()
                        .id(20L).majorId(5L).name("Class A").code("CA").build()
        ));

        assertThatThrownBy(() -> policy.checkBeforeDeleteMajor(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete major")
                .hasMessageContaining("referenced by one or more classes");
    }

    @Test
    void checkBeforeDeleteSchool_throwsException_whenReferencedByStudyPlan() {
        when(majorRepository.findBySchoolId(2L)).thenReturn(List.of());
        when(studyPlanRepository.findBySchoolId(2L)).thenReturn(List.of(
                StudyPlan.builder().id(1L).userId(10L).schoolId(2L).title("Plan A").build()
        ));

        assertThatThrownBy(() -> policy.checkBeforeDeleteSchool(2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete school")
                .hasMessageContaining("study plans");
    }

    @Test
    void checkBeforeDeleteMajor_throwsException_whenReferencedByStudyPlan() {
        when(classRepository.findByMajorId(3L)).thenReturn(List.of());
        when(studyPlanRepository.findByMajorId(3L)).thenReturn(List.of(
                StudyPlan.builder().id(2L).userId(10L).majorId(3L).title("Plan B").build()
        ));

        assertThatThrownBy(() -> policy.checkBeforeDeleteMajor(3L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete major")
                .hasMessageContaining("study plans");
    }

    @Test
    void checkBeforeDeleteClass_throwsException_whenReferencedByStudyPlan() {
        when(courseRepository.findByClassId(4L)).thenReturn(List.of());
        when(studyPlanRepository.findByClassId(4L)).thenReturn(List.of(
                StudyPlan.builder().id(3L).userId(10L).classId(4L).title("Plan C").build()
        ));

        assertThatThrownBy(() -> policy.checkBeforeDeleteClass(4L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete class")
                .hasMessageContaining("study plans");
    }

    @Test
    void checkBeforeDeleteSchool_succeeds_whenNoMajorsOrPlansReference() {
        when(majorRepository.findBySchoolId(7L)).thenReturn(List.of());
        when(studyPlanRepository.findBySchoolId(7L)).thenReturn(List.of());

        assertThatCode(() -> policy.checkBeforeDeleteSchool(7L)).doesNotThrowAnyException();
    }

    @Test
    void checkBeforeDeleteMajor_succeeds_whenNoClassesOrPlansReference() {
        when(classRepository.findByMajorId(8L)).thenReturn(List.of());
        when(studyPlanRepository.findByMajorId(8L)).thenReturn(List.of());

        assertThatCode(() -> policy.checkBeforeDeleteMajor(8L)).doesNotThrowAnyException();
    }

    @Test
    void checkBeforeDeleteClass_succeeds_whenNoCoursesOrPlansReference() {
        when(courseRepository.findByClassId(9L)).thenReturn(List.of());
        when(studyPlanRepository.findByClassId(9L)).thenReturn(List.of());

        assertThatCode(() -> policy.checkBeforeDeleteClass(9L)).doesNotThrowAnyException();
    }
}
