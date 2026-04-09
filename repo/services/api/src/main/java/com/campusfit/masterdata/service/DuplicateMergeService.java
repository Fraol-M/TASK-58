package com.campusfit.masterdata.service;

import com.campusfit.masterdata.entity.MergeOperation;
import com.campusfit.masterdata.repository.*;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import com.campusfit.study.repository.StudyPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DuplicateMergeService {

    private final TermRepository termRepository;
    private final SchoolRepository schoolRepository;
    private final MajorRepository majorRepository;
    private final AcademicClassRepository classRepository;
    private final CourseRepository courseRepository;
    private final MergeOperationRepository mergeOperationRepository;
    private final ChangeHistoryService changeHistoryService;
    private final StudyPlanRepository studyPlanRepository;

    public DuplicateMergeService(TermRepository termRepository,
                                 SchoolRepository schoolRepository,
                                 MajorRepository majorRepository,
                                 AcademicClassRepository classRepository,
                                 CourseRepository courseRepository,
                                 MergeOperationRepository mergeOperationRepository,
                                 ChangeHistoryService changeHistoryService,
                                 StudyPlanRepository studyPlanRepository) {
        this.termRepository = termRepository;
        this.schoolRepository = schoolRepository;
        this.majorRepository = majorRepository;
        this.classRepository = classRepository;
        this.courseRepository = courseRepository;
        this.mergeOperationRepository = mergeOperationRepository;
        this.changeHistoryService = changeHistoryService;
        this.studyPlanRepository = studyPlanRepository;
    }

    @Transactional
    public MergeOperation merge(String entityType, Long sourceId, Long targetId, Long mergedBy) {
        if (sourceId.equals(targetId)) {
            throw new BusinessException("Source and target cannot be the same entity");
        }

        switch (entityType.toUpperCase()) {
            case "SCHOOL":
                mergeSchool(sourceId, targetId, mergedBy);
                break;
            case "MAJOR":
                mergeMajor(sourceId, targetId, mergedBy);
                break;
            case "CLASS":
                mergeClass(sourceId, targetId, mergedBy);
                break;
            case "TERM":
                mergeTerm(sourceId, targetId, mergedBy);
                break;
            case "COURSE":
                mergeCourse(sourceId, targetId, mergedBy);
                break;
            default:
                throw new BusinessException("Unsupported entity type for merge: " + entityType);
        }

        MergeOperation operation = MergeOperation.builder()
                .entityType(entityType.toUpperCase())
                .sourceId(sourceId)
                .targetId(targetId)
                .mergedBy(mergedBy)
                .build();

        return mergeOperationRepository.save(operation);
    }

    private void mergeSchool(Long sourceId, Long targetId, Long userId) {
        schoolRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("School", sourceId));
        schoolRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("School", targetId));

        // Re-point all majors from source to target
        majorRepository.findBySchoolId(sourceId).forEach(major -> {
            changeHistoryService.logChange("MAJOR", major.getId(), "schoolId",
                    sourceId.toString(), targetId.toString(), userId);
            major.setSchoolId(targetId);
            majorRepository.save(major);
        });

        // Soft-delete source
        schoolRepository.findById(sourceId).ifPresent(school -> {
            school.setActive(false);
            schoolRepository.save(school);
        });
    }

    private void mergeMajor(Long sourceId, Long targetId, Long userId) {
        majorRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Major", sourceId));
        majorRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Major", targetId));

        classRepository.findByMajorId(sourceId).forEach(ac -> {
            changeHistoryService.logChange("CLASS", ac.getId(), "majorId",
                    sourceId.toString(), targetId.toString(), userId);
            ac.setMajorId(targetId);
            classRepository.save(ac);
        });

        majorRepository.findById(sourceId).ifPresent(major -> {
            major.setActive(false);
            majorRepository.save(major);
        });
    }

    private void mergeClass(Long sourceId, Long targetId, Long userId) {
        classRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicClass", sourceId));
        classRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicClass", targetId));

        courseRepository.findByClassId(sourceId).forEach(course -> {
            changeHistoryService.logChange("COURSE", course.getId(), "classId",
                    sourceId.toString(), targetId.toString(), userId);
            course.setClassId(targetId);
            courseRepository.save(course);
        });

        classRepository.findById(sourceId).ifPresent(ac -> {
            ac.setActive(false);
            classRepository.save(ac);
        });
    }

    private void mergeTerm(Long sourceId, Long targetId, Long userId) {
        termRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Term", sourceId));
        termRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Term", targetId));

        courseRepository.findByTermId(sourceId).forEach(course -> {
            changeHistoryService.logChange("COURSE", course.getId(), "termId",
                    sourceId.toString(), targetId.toString(), userId);
            course.setTermId(targetId);
            courseRepository.save(course);
        });

        // Repoint study plans referencing the source term
        studyPlanRepository.findByTermId(sourceId).forEach(plan -> {
            changeHistoryService.logChange("STUDY_PLAN", plan.getId(), "termId",
                    sourceId.toString(), targetId.toString(), userId);
            plan.setTermId(targetId);
            studyPlanRepository.save(plan);
        });

        termRepository.findById(sourceId).ifPresent(term -> {
            term.setActive(false);
            termRepository.save(term);
        });
    }

    private void mergeCourse(Long sourceId, Long targetId, Long userId) {
        courseRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", sourceId));
        courseRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", targetId));

        // Repoint study plans referencing the source course
        studyPlanRepository.findByCourseId(sourceId).forEach(plan -> {
            changeHistoryService.logChange("STUDY_PLAN", plan.getId(), "courseId",
                    sourceId.toString(), targetId.toString(), userId);
            plan.setCourseId(targetId);
            studyPlanRepository.save(plan);
        });

        // Soft-delete source course
        courseRepository.findById(sourceId).ifPresent(course -> {
            course.setActive(false);
            courseRepository.save(course);
        });
    }
}
