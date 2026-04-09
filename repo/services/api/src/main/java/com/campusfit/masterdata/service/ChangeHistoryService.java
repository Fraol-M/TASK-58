package com.campusfit.masterdata.service;

import com.campusfit.masterdata.dto.ChangeHistoryResponse;
import com.campusfit.masterdata.entity.ChangeHistory;
import com.campusfit.masterdata.repository.ChangeHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChangeHistoryService {

    private final ChangeHistoryRepository changeHistoryRepository;

    public ChangeHistoryService(ChangeHistoryRepository changeHistoryRepository) {
        this.changeHistoryRepository = changeHistoryRepository;
    }

    @Transactional
    public void logChange(String entityType, Long entityId, String field,
                          String oldValue, String newValue, Long changedBy) {
        ChangeHistory history = ChangeHistory.builder()
                .entityType(entityType)
                .entityId(entityId)
                .field(field)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .build();
        changeHistoryRepository.save(history);
    }

    @Transactional
    public void logCreate(String entityType, Long entityId, String name, Long changedBy) {
        logChange(entityType, entityId, "CREATE", null, name, changedBy);
    }

    @Transactional
    public void logDelete(String entityType, Long entityId, String name, Long changedBy) {
        logChange(entityType, entityId, "DELETE", name, null, changedBy);
    }

    @Transactional(readOnly = true)
    public List<ChangeHistoryResponse> getByEntity(String entityType, Long entityId) {
        return changeHistoryRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChangeHistoryResponse> getByEntityType(String entityType) {
        return changeHistoryRepository.findByEntityType(entityType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ChangeHistoryResponse toResponse(ChangeHistory h) {
        return ChangeHistoryResponse.builder()
                .id(h.getId())
                .entityType(h.getEntityType())
                .entityId(h.getEntityId())
                .field(h.getField())
                .oldValue(h.getOldValue())
                .newValue(h.getNewValue())
                .changedBy(h.getChangedBy())
                .changedAt(h.getChangedAt())
                .build();
    }
}
