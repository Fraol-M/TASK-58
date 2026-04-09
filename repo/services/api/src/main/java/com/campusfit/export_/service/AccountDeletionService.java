package com.campusfit.export_.service;

import com.campusfit.auth.entity.User;
import com.campusfit.auth.repository.SessionRepository;
import com.campusfit.auth.repository.UserRepository;
import com.campusfit.export_.entity.DeletionRequest;
import com.campusfit.export_.repository.DeletionRequestRepository;
import com.campusfit.fitness.repository.AssessmentRepository;
import com.campusfit.fitness.repository.CheckInRepository;
import com.campusfit.fitness.repository.GoalRepository;
import com.campusfit.shared.audit.AuditLogService;
import com.campusfit.shared.exception.ResourceNotFoundException;
import com.campusfit.study.repository.StudyPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccountDeletionService {

    private static final Logger log = LoggerFactory.getLogger(AccountDeletionService.class);

    private final DeletionRequestRepository deletionRequestRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final GoalRepository goalRepository;
    private final CheckInRepository checkInRepository;
    private final AssessmentRepository assessmentRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final AuditLogService auditLogService;

    public AccountDeletionService(DeletionRequestRepository deletionRequestRepository,
                                  UserRepository userRepository,
                                  SessionRepository sessionRepository,
                                  GoalRepository goalRepository,
                                  CheckInRepository checkInRepository,
                                  AssessmentRepository assessmentRepository,
                                  StudyPlanRepository studyPlanRepository,
                                  AuditLogService auditLogService) {
        this.deletionRequestRepository = deletionRequestRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.goalRepository = goalRepository;
        this.checkInRepository = checkInRepository;
        this.assessmentRepository = assessmentRepository;
        this.studyPlanRepository = studyPlanRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public DeletionRequest requestDeletion(Long userId) {
        DeletionRequest request = DeletionRequest.builder()
                .userId(userId)
                .status(DeletionRequest.DeletionStatus.PENDING)
                .build();

        request = deletionRequestRepository.save(request);
        auditLogService.log("DeletionRequest", request.getId(), "DELETION_REQUESTED", null, userId.toString());

        // Process immediately
        processDeletion(request);

        return request;
    }

    @Transactional
    public void processDeletion(DeletionRequest request) {
        request.setStatus(DeletionRequest.DeletionStatus.PROCESSING);
        deletionRequestRepository.save(request);

        Long userId = request.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Hard-delete all personal data records
        checkInRepository.deleteByUserId(userId);
        goalRepository.deleteByUserId(userId);
        assessmentRepository.deleteByUserId(userId);
        studyPlanRepository.deleteByUserId(userId);

        // Delete all sessions for this user
        sessionRepository.deleteByUserId(userId);

        // Hard-delete the user row itself.
        // Non-cascading FK references (inbound receipts, notifications, deletion requests)
        // are configured with ON DELETE SET NULL via V17 migration, so they become
        // de-identified audit artifacts with created_by/user_id = NULL.
        userRepository.delete(user);
        userRepository.flush();

        // Mark deletion complete — clear userId since the user row is gone
        // (the DB already set it to NULL via ON DELETE SET NULL)
        request.setUserId(null);
        request.setStatus(DeletionRequest.DeletionStatus.COMPLETED);
        request.setProcessedAt(LocalDateTime.now());
        deletionRequestRepository.save(request);

        auditLogService.log("DeletionRequest", request.getId(), "DELETION_COMPLETED",
                DeletionRequest.DeletionStatus.PROCESSING.name(), DeletionRequest.DeletionStatus.COMPLETED.name());
        log.info("Account deletion completed for user={}", "***" + (userId % 1000));
    }
}
