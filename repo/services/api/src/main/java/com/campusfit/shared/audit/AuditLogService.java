package com.campusfit.shared.audit;

import com.campusfit.shared.security.SecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(String entityType, Long entityId, String action, String oldValue, String newValue) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        AuditLog auditLog = AuditLog.builder()
            .entityType(entityType)
            .entityId(entityId)
            .action(action)
            .oldValue(oldValue)
            .newValue(newValue)
            .userId(userId)
            .build();

        auditLogRepository.save(auditLog);
    }
}
