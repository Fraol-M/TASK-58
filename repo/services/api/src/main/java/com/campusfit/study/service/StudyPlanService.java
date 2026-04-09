package com.campusfit.study.service;

import com.campusfit.masterdata.repository.*;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import com.campusfit.study.dto.StudyPlanRequest;
import com.campusfit.study.dto.StudyPlanResponse;
import com.campusfit.study.entity.StudyPlan;
import com.campusfit.study.repository.StudyPlanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final TermRepository termRepository;
    private final SchoolRepository schoolRepository;
    private final MajorRepository majorRepository;
    private final AcademicClassRepository classRepository;
    private final CourseRepository courseRepository;

    public StudyPlanService(StudyPlanRepository studyPlanRepository,
                            TermRepository termRepository,
                            SchoolRepository schoolRepository,
                            MajorRepository majorRepository,
                            AcademicClassRepository classRepository,
                            CourseRepository courseRepository) {
        this.studyPlanRepository = studyPlanRepository;
        this.termRepository = termRepository;
        this.schoolRepository = schoolRepository;
        this.majorRepository = majorRepository;
        this.classRepository = classRepository;
        this.courseRepository = courseRepository;
    }

    private void validateHierarchyReferences(StudyPlanRequest request) {
        if (request.getTermId() != null && !termRepository.existsById(request.getTermId())) {
            throw new BusinessException("Term with id " + request.getTermId() + " does not exist");
        }
        if (request.getSchoolId() != null && !schoolRepository.existsById(request.getSchoolId())) {
            throw new BusinessException("School with id " + request.getSchoolId() + " does not exist");
        }
        if (request.getMajorId() != null && !majorRepository.existsById(request.getMajorId())) {
            throw new BusinessException("Major with id " + request.getMajorId() + " does not exist");
        }
        if (request.getClassId() != null && !classRepository.existsById(request.getClassId())) {
            throw new BusinessException("Class with id " + request.getClassId() + " does not exist");
        }
        if (request.getCourseId() != null && !courseRepository.existsById(request.getCourseId())) {
            throw new BusinessException("Course with id " + request.getCourseId() + " does not exist");
        }
    }

    @Transactional
    public StudyPlanResponse create(Long userId, StudyPlanRequest request) {
        validateHierarchyReferences(request);

        StudyPlan plan = StudyPlan.builder()
                .userId(userId)
                .termId(request.getTermId())
                .schoolId(request.getSchoolId())
                .majorId(request.getMajorId())
                .classId(request.getClassId())
                .courseId(request.getCourseId())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(StudyPlan.PlanStatus.ACTIVE)
                .build();

        StudyPlan saved = studyPlanRepository.save(plan);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public StudyPlanResponse getById(Long planId, Long userId) {
        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyPlan", planId));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to view this study plan");
        }

        return toResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<StudyPlanResponse> getAllForUser(Long userId) {
        return studyPlanRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<StudyPlanResponse> getAllForUser(Long userId, Pageable pageable) {
        return studyPlanRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Transactional
    public StudyPlanResponse update(Long planId, Long userId, StudyPlanRequest request) {
        validateHierarchyReferences(request);

        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyPlan", planId));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to update this study plan");
        }

        plan.setTitle(request.getTitle());
        plan.setDescription(request.getDescription());
        plan.setTermId(request.getTermId());
        plan.setSchoolId(request.getSchoolId());
        plan.setMajorId(request.getMajorId());
        plan.setClassId(request.getClassId());
        plan.setCourseId(request.getCourseId());

        StudyPlan saved = studyPlanRepository.save(plan);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long planId, Long userId) {
        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyPlan", planId));

        if (!plan.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to delete this study plan");
        }

        plan.setStatus(StudyPlan.PlanStatus.ARCHIVED);
        studyPlanRepository.save(plan);
    }

    private StudyPlanResponse toResponse(StudyPlan plan) {
        return StudyPlanResponse.builder()
                .id(plan.getId())
                .userId(plan.getUserId())
                .termId(plan.getTermId())
                .schoolId(plan.getSchoolId())
                .majorId(plan.getMajorId())
                .classId(plan.getClassId())
                .courseId(plan.getCourseId())
                .title(plan.getTitle())
                .description(plan.getDescription())
                .status(plan.getStatus())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
