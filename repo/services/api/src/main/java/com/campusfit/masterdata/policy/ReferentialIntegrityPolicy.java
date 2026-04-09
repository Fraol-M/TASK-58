package com.campusfit.masterdata.policy;

import com.campusfit.masterdata.repository.*;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.study.repository.StudyPlanRepository;
import org.springframework.stereotype.Component;

@Component
public class ReferentialIntegrityPolicy {

    private final MajorRepository majorRepository;
    private final AcademicClassRepository classRepository;
    private final CourseRepository courseRepository;
    private final StudyPlanRepository studyPlanRepository;

    public ReferentialIntegrityPolicy(MajorRepository majorRepository,
                                      AcademicClassRepository classRepository,
                                      CourseRepository courseRepository,
                                      StudyPlanRepository studyPlanRepository) {
        this.majorRepository = majorRepository;
        this.classRepository = classRepository;
        this.courseRepository = courseRepository;
        this.studyPlanRepository = studyPlanRepository;
    }

    public void checkBeforeDeleteSchool(Long schoolId) {
        if (!majorRepository.findBySchoolId(schoolId).isEmpty()) {
            throw new BusinessException("Cannot delete school: it is referenced by one or more majors");
        }
        if (!studyPlanRepository.findBySchoolId(schoolId).isEmpty()) {
            throw new BusinessException("Cannot delete school: it is referenced by one or more study plans");
        }
    }

    public void checkBeforeDeleteMajor(Long majorId) {
        if (!classRepository.findByMajorId(majorId).isEmpty()) {
            throw new BusinessException("Cannot delete major: it is referenced by one or more classes");
        }
        if (!studyPlanRepository.findByMajorId(majorId).isEmpty()) {
            throw new BusinessException("Cannot delete major: it is referenced by one or more study plans");
        }
    }

    public void checkBeforeDeleteClass(Long classId) {
        if (!courseRepository.findByClassId(classId).isEmpty()) {
            throw new BusinessException("Cannot delete class: it is referenced by one or more courses");
        }
        if (!studyPlanRepository.findByClassId(classId).isEmpty()) {
            throw new BusinessException("Cannot delete class: it is referenced by one or more study plans");
        }
    }

    public void checkBeforeDeleteTerm(Long termId) {
        if (!courseRepository.findByTermId(termId).isEmpty()) {
            throw new BusinessException("Cannot delete term: it is referenced by one or more courses");
        }
        if (!studyPlanRepository.findByTermId(termId).isEmpty()) {
            throw new BusinessException("Cannot delete term: it is referenced by one or more study plans");
        }
    }

    public void checkBeforeDeleteCourse(Long courseId) {
        if (!studyPlanRepository.findByCourseId(courseId).isEmpty()) {
            throw new BusinessException("Cannot delete course: it is referenced by one or more study plans");
        }
    }
}
