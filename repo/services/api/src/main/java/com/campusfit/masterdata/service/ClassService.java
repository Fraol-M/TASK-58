package com.campusfit.masterdata.service;

import com.campusfit.masterdata.dto.MasterDataRequest;
import com.campusfit.masterdata.dto.MasterDataResponse;
import com.campusfit.masterdata.entity.AcademicClass;
import com.campusfit.masterdata.policy.EffectiveDatePolicy;
import com.campusfit.masterdata.policy.ReferentialIntegrityPolicy;
import com.campusfit.masterdata.repository.AcademicClassRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassService {

    private final AcademicClassRepository classRepository;
    private final EffectiveDatePolicy effectiveDatePolicy;
    private final ReferentialIntegrityPolicy referentialIntegrityPolicy;
    private final ChangeHistoryService changeHistoryService;

    public ClassService(AcademicClassRepository classRepository,
                        EffectiveDatePolicy effectiveDatePolicy,
                        ReferentialIntegrityPolicy referentialIntegrityPolicy,
                        ChangeHistoryService changeHistoryService) {
        this.classRepository = classRepository;
        this.effectiveDatePolicy = effectiveDatePolicy;
        this.referentialIntegrityPolicy = referentialIntegrityPolicy;
        this.changeHistoryService = changeHistoryService;
    }

    @Transactional
    public MasterDataResponse create(MasterDataRequest request, Long userId) {
        effectiveDatePolicy.validate(request.getEffectiveFrom(), request.getEffectiveTo());

        if (classRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Class with code '" + request.getCode() + "' already exists");
        }

        AcademicClass ac = AcademicClass.builder()
                .majorId(request.getMajorId())
                .code(request.getCode())
                .name(request.getName())
                .year(request.getYear() != null ? request.getYear() : 0)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(true)
                .build();

        AcademicClass saved = classRepository.save(ac);
        changeHistoryService.logCreate("CLASS", saved.getId(), saved.getName(), userId);
        return toResponse(saved);
    }

    @Transactional
    public MasterDataResponse update(Long id, MasterDataRequest request, Long userId) {
        effectiveDatePolicy.validate(request.getEffectiveFrom(), request.getEffectiveTo());

        AcademicClass ac = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicClass", id));

        String oldName = ac.getName();
        Long oldMajorId = ac.getMajorId();
        Integer oldYear = ac.getYear();
        ac.setName(request.getName());
        ac.setMajorId(request.getMajorId());
        ac.setYear(request.getYear() != null ? request.getYear() : ac.getYear());
        ac.setEffectiveFrom(request.getEffectiveFrom());
        ac.setEffectiveTo(request.getEffectiveTo());

        AcademicClass saved = classRepository.save(ac);

        if (!oldName.equals(request.getName())) {
            changeHistoryService.logChange("CLASS", id, "name", oldName, request.getName(), userId);
        }
        if (!java.util.Objects.equals(oldMajorId, request.getMajorId())) {
            changeHistoryService.logChange("CLASS", id, "majorId",
                    oldMajorId != null ? oldMajorId.toString() : null,
                    request.getMajorId() != null ? request.getMajorId().toString() : null, userId);
        }
        if (request.getYear() != null && !java.util.Objects.equals(oldYear, request.getYear())) {
            changeHistoryService.logChange("CLASS", id, "year",
                    oldYear != null ? oldYear.toString() : null, request.getYear().toString(), userId);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MasterDataResponse getById(Long id) {
        AcademicClass ac = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicClass", id));
        return toResponse(ac);
    }

    @Transactional(readOnly = true)
    public List<MasterDataResponse> getAll() {
        return classRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<MasterDataResponse> getAll(Pageable pageable) {
        return classRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        referentialIntegrityPolicy.checkBeforeDeleteClass(id);
        AcademicClass ac = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicClass", id));
        changeHistoryService.logDelete("CLASS", id, ac.getName(), userId);
        ac.setActive(false);
        classRepository.save(ac);
    }

    private MasterDataResponse toResponse(AcademicClass ac) {
        return MasterDataResponse.builder()
                .id(ac.getId())
                .code(ac.getCode())
                .name(ac.getName())
                .majorId(ac.getMajorId())
                .year(ac.getYear())
                .effectiveFrom(ac.getEffectiveFrom())
                .effectiveTo(ac.getEffectiveTo())
                .active(ac.isActive())
                .createdAt(ac.getCreatedAt())
                .updatedAt(ac.getUpdatedAt())
                .build();
    }
}
