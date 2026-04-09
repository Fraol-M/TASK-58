package com.campusfit.export_.service;

import com.campusfit.export_.dto.ExportRequest;
import com.campusfit.export_.dto.ExportResponse;
import com.campusfit.export_.entity.ExportJob;
import com.campusfit.export_.repository.ExportJobRepository;
import com.campusfit.auth.entity.User;
import com.campusfit.auth.repository.UserRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.fitness.service.AssessmentService;
import com.campusfit.fitness.service.GoalService;
import com.campusfit.fitness.service.CheckInService;
import com.campusfit.shared.audit.AuditLogService;
import com.campusfit.shared.encryption.FieldMasker;
import com.campusfit.study.dto.StudyExportData;
import com.campusfit.study.service.StudyExportImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private ExportJobRepository exportJobRepository;

    @Mock
    private StudyExportImportService studyExportImportService;

    @Mock
    private AssessmentService assessmentService;

    @Mock
    private GoalService goalService;

    @Mock
    private CheckInService checkInService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @TempDir
    Path tempDir;

    private ExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new ExportService(exportJobRepository, userRepository,
                studyExportImportService, assessmentService, goalService,
                checkInService, new FieldMasker(), auditLogService);
        try {
            java.lang.reflect.Field retField = ExportService.class.getDeclaredField("retentionDays");
            retField.setAccessible(true);
            retField.setInt(exportService, 30);

            java.lang.reflect.Field dirField = ExportService.class.getDeclaredField("exportBaseDir");
            dirField.setAccessible(true);
            dirField.set(exportService, tempDir.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createExport_passwordProtected_requiresPassword() {
        ExportRequest request = ExportRequest.builder()
                .exportType(ExportJob.ExportType.ACCOUNT_DATA)
                .passwordProtected(true)
                .exportPassword(null)
                .build();

        assertThatThrownBy(() -> exportService.createExportJob(100L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Export password is required");
    }

    @Test
    void createExport_withPassword_createsEncryptedFile() {
        ExportRequest request = ExportRequest.builder()
                .exportType(ExportJob.ExportType.STUDY_DATA)
                .passwordProtected(true)
                .exportPassword("securePass123")
                .build();

        when(exportJobRepository.save(any(ExportJob.class))).thenAnswer(inv -> {
            ExportJob job = inv.getArgument(0);
            if (job.getId() == null) job.setId(1L);
            return job;
        });

        when(studyExportImportService.exportData(100L)).thenReturn(
                StudyExportData.builder().userId(100L).plans(List.of()).build());

        ExportResponse result = exportService.createExportJob(100L, request);

        assertThat(result.isPasswordProtected()).isTrue();
        assertThat(result.getStatus()).isEqualTo(ExportJob.JobStatus.COMPLETED);
        assertThat(result.isDownloadReady()).isTrue();
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    void createExport_withoutPassword_throwsException() {
        ExportRequest request = ExportRequest.builder()
                .exportType(ExportJob.ExportType.STUDY_DATA)
                .passwordProtected(false)
                .build();

        assertThatThrownBy(() -> exportService.createExportJob(100L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("password is required");
    }

    @Test
    void createExport_accountData_profileFieldsAreRawInEncryptedPayload() throws Exception {
        // Export payload is AES-256 password-encrypted; raw profile data is stored
        // inside so that import can restore it. Log statements use masked values.
        ExportRequest request = ExportRequest.builder()
                .exportType(ExportJob.ExportType.ACCOUNT_DATA)
                .passwordProtected(true)
                .exportPassword("securePass123")
                .build();

        when(exportJobRepository.save(any(ExportJob.class))).thenAnswer(inv -> {
            ExportJob job = inv.getArgument(0);
            if (job.getId() == null) job.setId(2L);
            return job;
        });

        User user = User.builder()
                .id(100L)
                .username("john.doe")
                .email("john@example.com")
                .phone("+1234567890")
                .status(User.UserStatus.ACTIVE)
                .build();
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(studyExportImportService.exportData(100L)).thenReturn(
                StudyExportData.builder().userId(100L).plans(List.of()).build());
        when(assessmentService.getAllForUser(100L)).thenReturn(List.of());
        when(goalService.getAllForUser(100L)).thenReturn(List.of());

        ExportResponse result = exportService.createExportJob(100L, request);
        assertThat(result.getStatus()).isEqualTo(ExportJob.JobStatus.COMPLETED);

        Path userDir = tempDir.resolve("user-100");
        assertThat(userDir).exists();
        Path encFile = Files.list(userDir).findFirst().orElseThrow();
        byte[] fileBytes = Files.readAllBytes(encFile);
        Map<String, Object> decrypted = exportService.decryptExportFile(fileBytes, "securePass123");

        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) decrypted.get("profileData");
        assertThat(profile).isNotNull();
        // Raw values must be present in the encrypted payload so import can restore them
        assertThat(profile.get("email").toString()).isEqualTo("john@example.com");
        assertThat(profile.get("phone").toString()).isEqualTo("+1234567890");
        assertThat(profile.get("username").toString()).isEqualTo("john.doe");
    }

    @Test
    void createExport_setsExpiration() {
        ExportRequest request = ExportRequest.builder()
                .exportType(ExportJob.ExportType.STUDY_DATA)
                .passwordProtected(true)
                .exportPassword("secure123")
                .build();

        when(exportJobRepository.save(any(ExportJob.class))).thenAnswer(inv -> {
            ExportJob job = inv.getArgument(0);
            if (job.getId() == null) job.setId(3L);
            return job;
        });

        when(studyExportImportService.exportData(100L)).thenReturn(
                StudyExportData.builder().userId(100L).plans(List.of()).build());

        ExportResponse result = exportService.createExportJob(100L, request);

        assertThat(result.getExpiresAt()).isNotNull();
        assertThat(result.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(29));
        assertThat(result.getExpiresAt()).isBefore(LocalDateTime.now().plusDays(31));
    }
}
