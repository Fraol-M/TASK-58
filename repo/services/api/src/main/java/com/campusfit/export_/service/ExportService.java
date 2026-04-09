package com.campusfit.export_.service;

import com.campusfit.export_.dto.ExportRequest;
import com.campusfit.export_.dto.ExportResponse;
import com.campusfit.export_.entity.ExportJob;
import com.campusfit.export_.repository.ExportJobRepository;
import com.campusfit.auth.entity.User;
import com.campusfit.auth.repository.UserRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import com.campusfit.fitness.service.AssessmentService;
import com.campusfit.fitness.service.GoalService;
import com.campusfit.fitness.service.CheckInService;
import com.campusfit.fitness.dto.GoalResponse;
import com.campusfit.shared.audit.AuditLogService;
import com.campusfit.shared.encryption.FieldMasker;
import com.campusfit.study.service.StudyExportImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    @Value("${app.export.retention-days:30}")
    private int retentionDays;

    @Value("${app.export.base-dir:./exports}")
    private String exportBaseDir;

    private final ExportJobRepository exportJobRepository;
    private final UserRepository userRepository;
    private final StudyExportImportService studyExportImportService;
    private final AssessmentService assessmentService;
    private final GoalService goalService;
    private final CheckInService checkInService;
    private final FieldMasker fieldMasker;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public ExportService(ExportJobRepository exportJobRepository,
                         UserRepository userRepository,
                         StudyExportImportService studyExportImportService,
                         AssessmentService assessmentService,
                         GoalService goalService,
                         CheckInService checkInService,
                         FieldMasker fieldMasker,
                         AuditLogService auditLogService) {
        this.exportJobRepository = exportJobRepository;
        this.userRepository = userRepository;
        this.studyExportImportService = studyExportImportService;
        this.assessmentService = assessmentService;
        this.goalService = goalService;
        this.checkInService = checkInService;
        this.fieldMasker = fieldMasker;
        this.auditLogService = auditLogService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public ExportResponse createExportJob(Long userId, ExportRequest request) {
        if (request.getExportPassword() == null || request.getExportPassword().isBlank()) {
            throw new BusinessException("Export password is required. All exports must be password-protected.");
        }

        ExportJob job = ExportJob.builder()
                .userId(userId)
                .exportType(request.getExportType())
                .status(ExportJob.JobStatus.PENDING)
                .passwordProtected(true)
                .expiresAt(LocalDateTime.now().plusDays(retentionDays))
                .build();

        job = exportJobRepository.save(job);

        try {
            job.setStatus(ExportJob.JobStatus.PROCESSING);
            exportJobRepository.save(job);

            String exportData = generateExportData(userId, request.getExportType());

            log.info("Generating export type={} for user={}, passwordProtected={}",
                    request.getExportType(), fieldMasker.mask(String.valueOf(userId)), true);

            String filePath = writeExportFile(userId, job.getId(), exportData,
                    request.isPasswordProtected(), request.getExportPassword());

            job.setFilePath(filePath);
            job.setStatus(ExportJob.JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job = exportJobRepository.save(job);

            auditLogService.log("ExportJob", job.getId(), "EXPORT_COMPLETED",
                    request.getExportType().name(), ExportJob.JobStatus.COMPLETED.name());
            log.info("Export job {} completed for user={}", job.getId(), fieldMasker.mask(String.valueOf(userId)));
        } catch (Exception e) {
            job.setStatus(ExportJob.JobStatus.FAILED);
            exportJobRepository.save(job);
            log.error("Export job {} failed for user={}: {}", job.getId(), fieldMasker.mask(String.valueOf(userId)), e.getMessage());
            throw new BusinessException("Export failed: " + e.getMessage());
        }

        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public List<ExportResponse> getByUserId(Long userId) {
        return exportJobRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExportResponse getById(Long id, Long userId) {
        ExportJob job = exportJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExportJob", id));
        if (!job.getUserId().equals(userId)) {
            throw new BusinessException("Access denied to export job");
        }
        return toResponse(job);
    }

    public Path getExportFilePath(Long id, Long userId) {
        ExportJob job = exportJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExportJob", id));
        if (!job.getUserId().equals(userId)) {
            throw new BusinessException("Access denied to export job");
        }
        if (job.getStatus() != ExportJob.JobStatus.COMPLETED || job.getFilePath() == null) {
            throw new BusinessException("Export is not ready for download");
        }
        if (job.getExpiresAt() != null && job.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Export has expired");
        }
        return Paths.get(job.getFilePath());
    }

    private String generateExportData(Long userId, ExportJob.ExportType exportType) {
        try {
            java.util.Map<String, Object> exportBundle = new java.util.LinkedHashMap<>();
            exportBundle.put("exportedAt", LocalDateTime.now().toString());
            exportBundle.put("userId", fieldMasker.mask(String.valueOf(userId)));
            exportBundle.put("exportType", exportType.name());
            exportBundle.put("version", "2.0");

            switch (exportType) {
                case STUDY_DATA:
                    exportBundle.put("studyData", studyExportImportService.exportData(userId));
                    break;
                case FITNESS_DATA:
                    exportBundle.put("fitnessData", assessmentService.getAllForUser(userId));
                    exportBundle.put("goalsData", goalService.getAllForUser(userId));
                    addCheckInsForGoals(exportBundle, goalService.getAllForUser(userId), userId);
                    break;
                case ACCOUNT_DATA:
                default:
                    addProfileMetadata(exportBundle, userId);
                    exportBundle.put("studyData", studyExportImportService.exportData(userId));
                    exportBundle.put("fitnessData", assessmentService.getAllForUser(userId));
                    exportBundle.put("goalsData", goalService.getAllForUser(userId));
                    addCheckInsForGoals(exportBundle, goalService.getAllForUser(userId), userId);
                    break;
            }

            // Mask nested userId fields throughout the export bundle
            String json = objectMapper.writeValueAsString(exportBundle);
            return maskNestedUserIds(json, userId);
        } catch (Exception e) {
            throw new BusinessException("Failed to generate export data: " + e.getMessage());
        }
    }

    private void addProfileMetadata(java.util.Map<String, Object> exportBundle, Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            java.util.Map<String, Object> profile = new java.util.LinkedHashMap<>();
            // Raw identity fields are stored here because the export payload is AES-256
            // password-encrypted. Masking inside an encrypted bundle prevents migration
            // without adding security. Log statements use masked values instead.
            profile.put("username", user.getUsername());
            profile.put("email", user.getEmail());
            profile.put("phone", user.getPhone());
            profile.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
            exportBundle.put("profileData", profile);
            log.debug("Profile metadata added to export for user={}",
                    fieldMasker.mask(String.valueOf(userId)));
        });
    }

    private void addCheckInsForGoals(java.util.Map<String, Object> exportBundle,
                                     List<GoalResponse> goals, Long userId) {
        java.util.List<Object> allCheckIns = new java.util.ArrayList<>();
        for (GoalResponse goal : goals) {
            try {
                allCheckIns.addAll(checkInService.getByGoalId(goal.getId(), userId));
            } catch (Exception ignored) { }
        }
        exportBundle.put("checkInsData", allCheckIns);
    }

    private String maskNestedUserIds(String json, Long userId) {
        // Replace all occurrences of "userId":123 with 0 (redacted placeholder).
        // This is a privacy-redacted export; userId is not restore-safe.
        return json.replaceAll("\"userId\"\\s*:\\s*" + userId, "\"userId\":0");
    }

    private String writeExportFile(Long userId, Long jobId, String data,
                                    boolean passwordProtected, String password) throws IOException {
        Path userDir = Paths.get(exportBaseDir, "user-" + userId);
        Files.createDirectories(userDir);

        String fileName = "export-" + jobId + ".enc";
        Path filePath = userDir.resolve(fileName);

        byte[] content = data.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        content = encryptWithPassword(content, password);

        Files.write(filePath, content);
        return filePath.toString();
    }

    private byte[] encryptWithPassword(byte[] data, String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            byte[] iv = new byte[16];
            random.nextBytes(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKeySpec keySpec = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(data);

            // Prepend salt + IV to encrypted data
            byte[] result = new byte[salt.length + iv.length + encrypted.length];
            System.arraycopy(salt, 0, result, 0, salt.length);
            System.arraycopy(iv, 0, result, salt.length, iv.length);
            System.arraycopy(encrypted, 0, result, salt.length + iv.length, encrypted.length);
            return result;
        } catch (Exception e) {
            throw new BusinessException("Failed to encrypt export file: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public java.util.Map<String, Object> decryptExportFile(byte[] fileBytes, String password) {
        try {
            byte[] decrypted = decryptWithPassword(fileBytes, password);
            String json = new String(decrypted, java.nio.charset.StandardCharsets.UTF_8);
            return objectMapper.readValue(json, java.util.Map.class);
        } catch (Exception e) {
            throw new BusinessException("Failed to decrypt export file. Check that the password is correct.");
        }
    }

    private byte[] decryptWithPassword(byte[] data, String password) {
        try {
            byte[] salt = new byte[16];
            byte[] iv = new byte[16];
            System.arraycopy(data, 0, salt, 0, 16);
            System.arraycopy(data, 16, iv, 0, 16);
            byte[] encrypted = new byte[data.length - 32];
            System.arraycopy(data, 32, encrypted, 0, encrypted.length);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKeySpec keySpec = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new BusinessException("Decryption failed: " + e.getMessage());
        }
    }

    private ExportResponse toResponse(ExportJob job) {
        return ExportResponse.builder()
                .id(job.getId())
                .userId(job.getUserId())
                .exportType(job.getExportType())
                .status(job.getStatus())
                .downloadReady(job.getFilePath() != null && job.getStatus() == ExportJob.JobStatus.COMPLETED)
                .passwordProtected(job.isPasswordProtected())
                .expiresAt(job.getExpiresAt())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }
}
