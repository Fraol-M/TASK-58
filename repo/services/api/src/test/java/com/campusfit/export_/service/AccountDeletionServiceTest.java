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
import com.campusfit.study.repository.StudyPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountDeletionServiceTest {

    @Mock
    private DeletionRequestRepository deletionRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private CheckInRepository checkInRepository;
    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private StudyPlanRepository studyPlanRepository;

    @Mock
    private AuditLogService auditLogService;

    private AccountDeletionService accountDeletionService;

    @BeforeEach
    void setUp() {
        accountDeletionService = new AccountDeletionService(
                deletionRequestRepository, userRepository, sessionRepository,
                goalRepository, checkInRepository, assessmentRepository, studyPlanRepository,
                auditLogService);
    }

    @Test
    void deleteAccount_hardDeletesUserRow() {
        User user = User.builder()
                .id(42L)
                .username("john.doe")
                .email("john@example.com")
                .phone("+1234567890")
                .passwordHash("hashed_pw")
                .status(User.UserStatus.ACTIVE)
                .build();

        DeletionRequest request = DeletionRequest.builder()
                .id(1L)
                .userId(42L)
                .status(DeletionRequest.DeletionStatus.PENDING)
                .build();

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(deletionRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        accountDeletionService.processDeletion(request);

        // User row is hard-deleted, not anonymized
        verify(userRepository).delete(user);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteAccount_hardDeletesAllPersonalData() {
        User user = User.builder()
                .id(42L)
                .username("john.doe")
                .email("john@example.com")
                .passwordHash("hashed_pw")
                .status(User.UserStatus.ACTIVE)
                .build();

        DeletionRequest request = DeletionRequest.builder()
                .id(1L).userId(42L)
                .status(DeletionRequest.DeletionStatus.PENDING).build();

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(deletionRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        accountDeletionService.processDeletion(request);

        verify(checkInRepository).deleteByUserId(42L);
        verify(goalRepository).deleteByUserId(42L);
        verify(assessmentRepository).deleteByUserId(42L);
        verify(studyPlanRepository).deleteByUserId(42L);
    }

    @Test
    void deleteAccount_deletesSessions() {
        User user = User.builder()
                .id(42L)
                .username("john.doe")
                .email("john@example.com")
                .passwordHash("hashed_pw")
                .status(User.UserStatus.ACTIVE)
                .build();

        DeletionRequest request = DeletionRequest.builder()
                .id(1L).userId(42L)
                .status(DeletionRequest.DeletionStatus.PENDING).build();

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(deletionRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        accountDeletionService.processDeletion(request);

        verify(sessionRepository).deleteByUserId(42L);
    }

    @Test
    void deleteAccount_retainsDeletionRequestAsAuditArtifact() {
        User user = User.builder()
                .id(42L)
                .username("john.doe")
                .email("john@example.com")
                .passwordHash("hashed_pw")
                .status(User.UserStatus.ACTIVE)
                .build();

        DeletionRequest request = DeletionRequest.builder()
                .id(1L).userId(42L)
                .status(DeletionRequest.DeletionStatus.PENDING).build();

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(deletionRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        accountDeletionService.processDeletion(request);

        // Deletion request is retained as a de-identified audit artifact
        ArgumentCaptor<DeletionRequest> requestCaptor = ArgumentCaptor.forClass(DeletionRequest.class);
        verify(deletionRequestRepository, atLeast(2)).save(requestCaptor.capture());

        DeletionRequest completedRequest = requestCaptor.getAllValues().stream()
                .filter(r -> r.getStatus() == DeletionRequest.DeletionStatus.COMPLETED)
                .findFirst()
                .orElse(null);

        assertThat(completedRequest).isNotNull();
        assertThat(completedRequest.getProcessedAt()).isNotNull();

        // User row is deleted, not retained
        verify(userRepository).delete(user);
    }
}
