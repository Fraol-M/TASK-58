package com.campusfit.masterdata.service;

import com.campusfit.masterdata.dto.MasterDataRequest;
import com.campusfit.masterdata.dto.MasterDataResponse;
import com.campusfit.masterdata.entity.Major;
import com.campusfit.masterdata.policy.EffectiveDatePolicy;
import com.campusfit.masterdata.policy.ReferentialIntegrityPolicy;
import com.campusfit.masterdata.repository.MajorRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MajorService {

    private final MajorRepository majorRepository;
    private final EffectiveDatePolicy effectiveDatePolicy;
    private final ReferentialIntegrityPolicy referentialIntegrityPolicy;
    private final ChangeHistoryService changeHistoryService;

    public MajorService(MajorRepository majorRepository,
                        EffectiveDatePolicy effectiveDatePolicy,
                        ReferentialIntegrityPolicy referentialIntegrityPolicy,
                        ChangeHistoryService changeHistoryService) {
        this.majorRepository = majorRepository;
        this.effectiveDatePolicy = effectiveDatePolicy;
        this.referentialIntegrityPolicy = referentialIntegrityPolicy;
        this.changeHistoryService = changeHistoryService;
    }

    @Transactional
    public MasterDataResponse create(MasterDataRequest request, Long userId) {
        effectiveDatePolicy.validate(request.getEffectiveFrom(), request.getEffectiveTo());

        if (majorRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Major with code '" + request.getCode() + "' already exists");
        }

        Major major = Major.builder()
                .schoolId(request.getSchoolId())
                .code(request.getCode())
                .name(request.getName())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(true)
                .build();

        Major saved = majorRepository.save(major);
        changeHistoryService.logCreate("MAJOR", saved.getId(), saved.getName(), userId);
        return toResponse(saved);
    }

    @Transactional
    public MasterDataResponse update(Long id, MasterDataRequest request, Long userId) {
        effectiveDatePolicy.validate(request.getEffectiveFrom(), request.getEffectiveTo());

        Major major = majorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Major", id));

        String oldName = major.getName();
        Long oldSchoolId = major.getSchoolId();
        major.setName(request.getName());
        major.setSchoolId(request.getSchoolId());
        major.setEffectiveFrom(request.getEffectiveFrom());
        major.setEffectiveTo(request.getEffectiveTo());

        Major saved = majorRepository.save(major);

        if (!oldName.equals(request.getName())) {
            changeHistoryService.logChange("MAJOR", id, "name", oldName, request.getName(), userId);
        }
        if (!java.util.Objects.equals(oldSchoolId, request.getSchoolId())) {
            changeHistoryService.logChange("MAJOR", id, "schoolId",
                    oldSchoolId != null ? oldSchoolId.toString() : null,
                    request.getSchoolId() != null ? request.getSchoolId().toString() : null, userId);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MasterDataResponse getById(Long id) {
        Major major = majorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Major", id));
        return toResponse(major);
    }

    @Transactional(readOnly = true)
    public List<MasterDataResponse> getAll() {
        return majorRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<MasterDataResponse> getAll(Pageable pageable) {
        return majorRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        referentialIntegrityPolicy.checkBeforeDeleteMajor(id);
        Major major = majorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Major", id));
        changeHistoryService.logDelete("MAJOR", id, major.getName(), userId);
        major.setActive(false);
        majorRepository.save(major);
    }

    private MasterDataResponse toResponse(Major m) {
        return MasterDataResponse.builder()
                .id(m.getId())
                .code(m.getCode())
                .name(m.getName())
                .schoolId(m.getSchoolId())
                .effectiveFrom(m.getEffectiveFrom())
                .effectiveTo(m.getEffectiveTo())
                .active(m.isActive())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
