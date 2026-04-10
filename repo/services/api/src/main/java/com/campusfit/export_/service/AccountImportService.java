package com.campusfit.export_.service;

import com.campusfit.auth.entity.User;
import com.campusfit.auth.repository.UserRepository;
import com.campusfit.fitness.dto.AssessmentRequest;
import com.campusfit.fitness.dto.CheckInRequest;
import com.campusfit.fitness.dto.GoalRequest;
import com.campusfit.fitness.dto.GoalResponse;
import com.campusfit.fitness.entity.Goal;
import com.campusfit.fitness.service.AssessmentService;
import com.campusfit.fitness.service.CheckInService;
import com.campusfit.fitness.service.GoalService;
import com.campusfit.study.dto.StudyExportData;
import com.campusfit.study.service.StudyExportImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountImportService {

    private static final Logger log = LoggerFactory.getLogger(AccountImportService.class);

    private final UserRepository userRepository;
    private final StudyExportImportService studyExportImportService;
    private final AssessmentService assessmentService;
    private final GoalService goalService;
    private final CheckInService checkInService;
    private final ObjectMapper objectMapper;

    public AccountImportService(UserRepository userRepository,
                                StudyExportImportService studyExportImportService,
                                AssessmentService assessmentService,
                                GoalService goalService,
                                CheckInService checkInService) {
        this.userRepository = userRepository;
        this.studyExportImportService = studyExportImportService;
        this.assessmentService = assessmentService;
        this.goalService = goalService;
        this.checkInService = checkInService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public String importAccountData(Long userId, Map<String, Object> data) {
        int studyCount = 0;
        int fitnessCount = 0;
        int goalCount = 0;
        int checkInCount = 0;

        // Restore profile metadata if present
        if (data.containsKey("profileData")) {
            try {
                Map<String, Object> profile = (Map<String, Object>) data.get("profileData");
                restoreProfile(userId, profile);
            } catch (Exception e) {
                log.warn("Failed to restore profile metadata: {}", e.getMessage());
            }
        }

        // Import study data if present
        if (data.containsKey("studyData")) {
            try {
                StudyExportData studyData = objectMapper.convertValue(data.get("studyData"), StudyExportData.class);
                studyExportImportService.importData(userId, studyData);
                studyCount++;
            } catch (Exception e) {
                log.warn("Failed to import study data: {}", e.getMessage());
            }
        }

        // Import fitness assessments if present
        if (data.containsKey("fitnessData")) {
            try {
                List<Map<String, Object>> fitnessDataList = (List<Map<String, Object>>) data.get("fitnessData");
                for (Map<String, Object> assessmentData : fitnessDataList) {
                    AssessmentRequest request = objectMapper.convertValue(assessmentData, AssessmentRequest.class);
                    assessmentService.createOrUpdate(userId, request);
                    fitnessCount++;
                }
            } catch (Exception e) {
                log.warn("Failed to import fitness data: {}", e.getMessage());
            }
        }

        // Import goals if present — build old→new ID map for check-in remapping
        Map<Long, Long> goalIdRemap = new HashMap<>();
        if (data.containsKey("goalsData")) {
            try {
                List<Map<String, Object>> goalsDataList = (List<Map<String, Object>>) data.get("goalsData");
                for (Map<String, Object> goalData : goalsDataList) {
                    Long oldGoalId = goalData.get("id") != null
                            ? ((Number) goalData.get("id")).longValue() : null;

                    GoalRequest request = GoalRequest.builder()
                            .goalType(goalData.get("goalType") != null
                                    ? Goal.GoalType.valueOf(goalData.get("goalType").toString())
                                    : Goal.GoalType.WEIGHT_LOSS)
                            .description(goalData.get("description") != null
                                    ? goalData.get("description").toString() : null)
                            .targetValue(goalData.get("targetValue") != null
                                    ? new java.math.BigDecimal(goalData.get("targetValue").toString())
                                    : java.math.BigDecimal.ZERO)
                            .unit(goalData.get("unit") != null
                                    ? goalData.get("unit").toString() : "lbs")
                            .startDate(goalData.get("startDate") != null
                                    ? java.time.LocalDate.parse(goalData.get("startDate").toString())
                                    : java.time.LocalDate.now())
                            .targetDate(goalData.get("targetDate") != null
                                    ? java.time.LocalDate.parse(goalData.get("targetDate").toString())
                                    : java.time.LocalDate.now().plusWeeks(12))
                            .build();
                    GoalResponse created = goalService.create(userId, request);

                    if (oldGoalId != null) {
                        goalIdRemap.put(oldGoalId, created.getId());
                    }
                    goalCount++;
                }
            } catch (Exception e) {
                log.warn("Failed to import goals data: {}", e.getMessage());
            }
        }

        // Import check-ins if present — remap goalId from old to new
        if (data.containsKey("checkInsData")) {
            try {
                List<Map<String, Object>> checkInsDataList = (List<Map<String, Object>>) data.get("checkInsData");
                for (Map<String, Object> checkInData : checkInsDataList) {
                    Long oldGoalId = checkInData.get("goalId") != null
                            ? ((Number) checkInData.get("goalId")).longValue() : null;
                    if (oldGoalId == null) continue;

                    Long newGoalId = goalIdRemap.getOrDefault(oldGoalId, oldGoalId);
                    CheckInRequest request = objectMapper.convertValue(checkInData, CheckInRequest.class);
                    checkInService.create(newGoalId, userId, request);
                    checkInCount++;
                }
            } catch (Exception e) {
                log.warn("Failed to import check-ins data: {}", e.getMessage());
            }
        }

        return String.format("Import completed. Study: %d, Assessments: %d, Goals: %d, Check-ins: %d",
                studyCount, fitnessCount, goalCount, checkInCount);
    }

    private void restoreProfile(Long userId, Map<String, Object> profile) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        // Export payloads store raw profile values inside the AES-256 encrypted file.
        // A safety guard is kept for forward-compatibility with any future format changes.
        Object email = profile.get("email");
        if (email instanceof String && !((String) email).isBlank()) {
            if (!isMasked((String) email)) {
                user.setEmail((String) email);
            } else {
                log.warn("Skipping masked email field in import payload for userId=***{}", userId % 1000);
            }
        }
        Object phone = profile.get("phone");
        if (phone instanceof String && !((String) phone).isBlank()) {
            if (!isMasked((String) phone)) {
                user.setPhone((String) phone);
            } else {
                log.warn("Skipping masked phone field in import payload for userId=***{}", userId % 1000);
            }
        }
        userRepository.save(user);
    }

    private boolean isMasked(String value) {
        return value.contains("***") || value.matches("\\*+");
    }
}
