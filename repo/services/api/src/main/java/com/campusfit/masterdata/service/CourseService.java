package com.campusfit.masterdata.service;

import com.campusfit.masterdata.dto.MasterDataRequest;
import com.campusfit.masterdata.dto.MasterDataResponse;
import com.campusfit.masterdata.entity.Course;
import com.campusfit.masterdata.policy.EffectiveDatePolicy;
import com.campusfit.masterdata.policy.ReferentialIntegrityPolicy;
import com.campusfit.masterdata.repository.CourseRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final EffectiveDatePolicy effectiveDatePolicy;
    private final ReferentialIntegrityPolicy referentialIntegrityPolicy;
    private final ChangeHistoryService changeHistoryService;

    public CourseService(CourseRepository courseRepository,
                         EffectiveDatePolicy effectiveDatePolicy,
                         ReferentialIntegrityPolicy referentialIntegrityPolicy,
                         ChangeHistoryService changeHistoryService) {
        this.courseRepository = courseRepository;
        this.effectiveDatePolicy = effectiveDatePolicy;
        this.referentialIntegrityPolicy = referentialIntegrityPolicy;
        this.changeHistoryService = changeHistoryService;
    }

    @Transactional
    public MasterDataResponse create(MasterDataRequest request, Long userId) {
        effectiveDatePolicy.validate(request.getEffectiveFrom(), request.getEffectiveTo());

        if (courseRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Course with code '" + request.getCode() + "' already exists");
        }

        Course course = Course.builder()
                .classId(request.getClassId())
                .termId(request.getTermId())
                .code(request.getCode())
                .name(request.getName())
                .credits(request.getCredits() != null ? request.getCredits() : 0)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(true)
                .build();

        Course saved = courseRepository.save(course);
        changeHistoryService.logCreate("COURSE", saved.getId(), saved.getName(), userId);
        return toResponse(saved);
    }

    @Transactional
    public MasterDataResponse update(Long id, MasterDataRequest request, Long userId) {
        effectiveDatePolicy.validate(request.getEffectiveFrom(), request.getEffectiveTo());

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        String oldName = course.getName();
        Long oldClassId = course.getClassId();
        Long oldTermId = course.getTermId();
        Integer oldCredits = course.getCredits();
        course.setName(request.getName());
        course.setClassId(request.getClassId());
        course.setTermId(request.getTermId());
        course.setCredits(request.getCredits() != null ? request.getCredits() : course.getCredits());
        course.setEffectiveFrom(request.getEffectiveFrom());
        course.setEffectiveTo(request.getEffectiveTo());

        Course saved = courseRepository.save(course);

        if (!oldName.equals(request.getName())) {
            changeHistoryService.logChange("COURSE", id, "name", oldName, request.getName(), userId);
        }
        if (!java.util.Objects.equals(oldClassId, request.getClassId())) {
            changeHistoryService.logChange("COURSE", id, "classId",
                    oldClassId != null ? oldClassId.toString() : null,
                    request.getClassId() != null ? request.getClassId().toString() : null, userId);
        }
        if (!java.util.Objects.equals(oldTermId, request.getTermId())) {
            changeHistoryService.logChange("COURSE", id, "termId",
                    oldTermId != null ? oldTermId.toString() : null,
                    request.getTermId() != null ? request.getTermId().toString() : null, userId);
        }
        if (request.getCredits() != null && !java.util.Objects.equals(oldCredits, request.getCredits())) {
            changeHistoryService.logChange("COURSE", id, "credits",
                    oldCredits != null ? oldCredits.toString() : null, request.getCredits().toString(), userId);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MasterDataResponse getById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        return toResponse(course);
    }

    @Transactional(readOnly = true)
    public List<MasterDataResponse> getAll() {
        return courseRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<MasterDataResponse> getAll(Pageable pageable) {
        return courseRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        referentialIntegrityPolicy.checkBeforeDeleteCourse(id);
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        changeHistoryService.logDelete("COURSE", id, course.getName(), userId);
        course.setActive(false);
        courseRepository.save(course);
    }

    private MasterDataResponse toResponse(Course c) {
        return MasterDataResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .classId(c.getClassId())
                .termId(c.getTermId())
                .credits(c.getCredits())
                .effectiveFrom(c.getEffectiveFrom())
                .effectiveTo(c.getEffectiveTo())
                .active(c.isActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
