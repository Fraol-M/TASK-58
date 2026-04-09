package com.campusfit.masterdata.service;

import com.campusfit.masterdata.dto.MasterDataRequest;
import com.campusfit.masterdata.dto.MasterDataResponse;
import com.campusfit.masterdata.entity.School;
import com.campusfit.masterdata.policy.EffectiveDatePolicy;
import com.campusfit.masterdata.policy.ReferentialIntegrityPolicy;
import com.campusfit.masterdata.repository.SchoolRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final EffectiveDatePolicy effectiveDatePolicy;
    private final ReferentialIntegrityPolicy referentialIntegrityPolicy;
    private final ChangeHistoryService changeHistoryService;

    public SchoolService(SchoolRepository schoolRepository,
                         EffectiveDatePolicy effectiveDatePolicy,
                         ReferentialIntegrityPolicy referentialIntegrityPolicy,
                         ChangeHistoryService changeHistoryService) {
        this.schoolRepository = schoolRepository;
        this.effectiveDatePolicy = effectiveDatePolicy;
        this.referentialIntegrityPolicy = referentialIntegrityPolicy;
        this.changeHistoryService = changeHistoryService;
    }

    @Transactional
    public MasterDataResponse create(MasterDataRequest request, Long userId) {
        effectiveDatePolicy.validate(request.getEffectiveFrom(), request.getEffectiveTo());

        if (schoolRepository.existsByCode(request.getCode())) {
            throw new BusinessException("School with code '" + request.getCode() + "' already exists");
        }

        School school = School.builder()
                .code(request.getCode())
                .name(request.getName())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(true)
                .build();

        School saved = schoolRepository.save(school);
        changeHistoryService.logCreate("SCHOOL", saved.getId(), saved.getName(), userId);
        return toResponse(saved);
    }

    @Transactional
    public MasterDataResponse update(Long id, MasterDataRequest request, Long userId) {
        effectiveDatePolicy.validate(request.getEffectiveFrom(), request.getEffectiveTo());

        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School", id));

        String oldName = school.getName();
        String oldEffFrom = school.getEffectiveFrom() != null ? school.getEffectiveFrom().toString() : null;
        String oldEffTo = school.getEffectiveTo() != null ? school.getEffectiveTo().toString() : null;

        school.setName(request.getName());
        school.setEffectiveFrom(request.getEffectiveFrom());
        school.setEffectiveTo(request.getEffectiveTo());

        School saved = schoolRepository.save(school);

        if (!oldName.equals(request.getName())) {
            changeHistoryService.logChange("SCHOOL", id, "name", oldName, request.getName(), userId);
        }
        String newEffFrom = request.getEffectiveFrom() != null ? request.getEffectiveFrom().toString() : null;
        if (!java.util.Objects.equals(oldEffFrom, newEffFrom)) {
            changeHistoryService.logChange("SCHOOL", id, "effectiveFrom", oldEffFrom, newEffFrom, userId);
        }
        String newEffTo = request.getEffectiveTo() != null ? request.getEffectiveTo().toString() : null;
        if (!java.util.Objects.equals(oldEffTo, newEffTo)) {
            changeHistoryService.logChange("SCHOOL", id, "effectiveTo", oldEffTo, newEffTo, userId);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MasterDataResponse getById(Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School", id));
        return toResponse(school);
    }

    @Transactional(readOnly = true)
    public List<MasterDataResponse> getAll() {
        return schoolRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<MasterDataResponse> getAll(Pageable pageable) {
        return schoolRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        referentialIntegrityPolicy.checkBeforeDeleteSchool(id);
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School", id));
        changeHistoryService.logDelete("SCHOOL", id, school.getName(), userId);
        school.setActive(false);
        schoolRepository.save(school);
    }

    private MasterDataResponse toResponse(School s) {
        return MasterDataResponse.builder()
                .id(s.getId())
                .code(s.getCode())
                .name(s.getName())
                .effectiveFrom(s.getEffectiveFrom())
                .effectiveTo(s.getEffectiveTo())
                .active(s.isActive())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
