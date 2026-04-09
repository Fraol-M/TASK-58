package com.campusfit.masterdata.service;

import com.campusfit.masterdata.dto.MasterDataRequest;
import com.campusfit.masterdata.dto.MasterDataResponse;
import com.campusfit.masterdata.entity.Term;
import com.campusfit.masterdata.policy.EffectiveDatePolicy;
import com.campusfit.masterdata.policy.ReferentialIntegrityPolicy;
import com.campusfit.masterdata.repository.TermRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TermService {

    private final TermRepository termRepository;
    private final EffectiveDatePolicy effectiveDatePolicy;
    private final ReferentialIntegrityPolicy referentialIntegrityPolicy;
    private final ChangeHistoryService changeHistoryService;

    public TermService(TermRepository termRepository,
                       EffectiveDatePolicy effectiveDatePolicy,
                       ReferentialIntegrityPolicy referentialIntegrityPolicy,
                       ChangeHistoryService changeHistoryService) {
        this.termRepository = termRepository;
        this.effectiveDatePolicy = effectiveDatePolicy;
        this.referentialIntegrityPolicy = referentialIntegrityPolicy;
        this.changeHistoryService = changeHistoryService;
    }

    @CacheEvict(value = "masterdata_terms", allEntries = true)
    @Transactional
    public MasterDataResponse create(MasterDataRequest request, Long userId) {
        effectiveDatePolicy.validate(request.getEffectiveFrom(), request.getEffectiveTo());

        if (termRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Term with code '" + request.getCode() + "' already exists");
        }

        Term term = Term.builder()
                .code(request.getCode())
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(true)
                .build();

        Term saved = termRepository.save(term);
        changeHistoryService.logCreate("TERM", saved.getId(), saved.getName(), userId);
        return toResponse(saved);
    }

    @Transactional
    public MasterDataResponse update(Long id, MasterDataRequest request, Long userId) {
        effectiveDatePolicy.validate(request.getEffectiveFrom(), request.getEffectiveTo());

        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term", id));

        String oldName = term.getName();
        String oldStartDate = term.getStartDate() != null ? term.getStartDate().toString() : null;
        String oldEndDate = term.getEndDate() != null ? term.getEndDate().toString() : null;

        term.setName(request.getName());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());
        term.setEffectiveFrom(request.getEffectiveFrom());
        term.setEffectiveTo(request.getEffectiveTo());

        Term saved = termRepository.save(term);

        if (!oldName.equals(request.getName())) {
            changeHistoryService.logChange("TERM", id, "name", oldName, request.getName(), userId);
        }
        String newStartDate = request.getStartDate() != null ? request.getStartDate().toString() : null;
        if (!java.util.Objects.equals(oldStartDate, newStartDate)) {
            changeHistoryService.logChange("TERM", id, "startDate", oldStartDate, newStartDate, userId);
        }
        String newEndDate = request.getEndDate() != null ? request.getEndDate().toString() : null;
        if (!java.util.Objects.equals(oldEndDate, newEndDate)) {
            changeHistoryService.logChange("TERM", id, "endDate", oldEndDate, newEndDate, userId);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MasterDataResponse getById(Long id) {
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term", id));
        return toResponse(term);
    }

    @Cacheable("masterdata_terms")
    @Transactional(readOnly = true)
    public List<MasterDataResponse> getAll() {
        return termRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<MasterDataResponse> getAll(Pageable pageable) {
        return termRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        referentialIntegrityPolicy.checkBeforeDeleteTerm(id);
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term", id));
        changeHistoryService.logDelete("TERM", id, term.getName(), userId);
        term.setActive(false);
        termRepository.save(term);
    }

    private MasterDataResponse toResponse(Term t) {
        return MasterDataResponse.builder()
                .id(t.getId())
                .code(t.getCode())
                .name(t.getName())
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .effectiveFrom(t.getEffectiveFrom())
                .effectiveTo(t.getEffectiveTo())
                .active(t.isActive())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
