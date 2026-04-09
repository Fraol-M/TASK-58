package com.campusfit.fitness.service;

import com.campusfit.fitness.dto.AssessmentRequest;
import com.campusfit.fitness.dto.AssessmentResponse;
import com.campusfit.fitness.entity.Assessment;
import com.campusfit.fitness.repository.AssessmentRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    private static final Logger log = LoggerFactory.getLogger(AssessmentService.class);

    private final AssessmentRepository assessmentRepository;
    private final ObjectMapper objectMapper;

    public AssessmentService(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public AssessmentResponse createOrUpdate(Long userId, AssessmentRequest request) {
        String encryptedMetrics = buildEncryptedMetrics(request);

        Assessment assessment = Assessment.builder()
                .userId(userId)
                .assessmentType("INITIAL")
                .metricsEncrypted(encryptedMetrics)
                .assessmentDate(LocalDate.now())
                .notes(request.getNotes())
                .build();

        List<Assessment> existing = assessmentRepository.findByUserId(userId);
        if (!existing.isEmpty()) {
            assessment.setAssessmentType("PERIODIC");
        }

        Assessment saved = assessmentRepository.save(assessment);
        return toResponse(saved, request);
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getLatest(Long userId) {
        Assessment assessment = assessmentRepository.findTopByUserIdOrderByAssessmentDateDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", userId));
        return toResponse(assessment, null);
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAllForUser(Long userId) {
        return assessmentRepository.findByUserId(userId).stream()
                .map(a -> toResponse(a, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public AssessmentResponse update(Long assessmentId, Long userId, AssessmentRequest request) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", assessmentId));

        if (!assessment.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to update this assessment");
        }

        assessment.setMetricsEncrypted(buildEncryptedMetrics(request));
        assessment.setNotes(request.getNotes());

        Assessment saved = assessmentRepository.save(assessment);
        return toResponse(saved, request);
    }

    private String buildEncryptedMetrics(AssessmentRequest request) {
        try {
            Map<String, Object> metrics = new LinkedHashMap<>();
            metrics.put("heightFeet", request.getHeightFeet());
            metrics.put("heightInches", request.getHeightInches());
            metrics.put("weightLbs", request.getWeightLbs());
            metrics.put("bodyFatPercent", request.getBodyFatPercent());
            metrics.put("waist", request.getWaist());
            metrics.put("chest", request.getChest());
            metrics.put("arm", request.getArm());
            return objectMapper.writeValueAsString(metrics);
        } catch (Exception e) {
            throw new BusinessException("Failed to serialize metrics for encryption: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseEncryptedMetrics(String encrypted) {
        if (encrypted == null || encrypted.isBlank() || "PENDING_MIGRATION".equals(encrypted)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(encrypted, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse encrypted metrics: {}", e.getMessage());
            return Map.of();
        }
    }

    private AssessmentResponse toResponse(Assessment a, AssessmentRequest originalRequest) {
        // Read metrics from encrypted field (authoritative source)
        Map<String, Object> metrics = parseEncryptedMetrics(a.getMetricsEncrypted());

        int heightFeet = getInt(metrics, "heightFeet", a.getHeightFeet());
        int heightInches = getInt(metrics, "heightInches", a.getHeightInches());
        double weightLbs = getDouble(metrics, "weightLbs", a.getWeightLbs());
        Double bodyFatPercent = getOptionalDouble(metrics, "bodyFatPercent", a.getBodyFatPercent());
        Double waistInches = getOptionalDouble(metrics, "waist", a.getWaistInches());
        Double chestInches = getOptionalDouble(metrics, "chest", a.getChestInches());
        Double armInches = getOptionalDouble(metrics, "arm", a.getArmInches());

        // If we have the original request, prefer those values for immediate response
        if (originalRequest != null) {
            heightFeet = originalRequest.getHeightFeet();
            heightInches = originalRequest.getHeightInches();
            weightLbs = originalRequest.getWeightLbs();
            bodyFatPercent = originalRequest.getBodyFatPercent();
            waistInches = originalRequest.getWaist();
            chestInches = originalRequest.getChest();
            armInches = originalRequest.getArm();
        }

        String formattedHeight = heightFeet + "'" + heightInches + "\"";

        return AssessmentResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .assessmentType(a.getAssessmentType())
                .heightFeet(heightFeet)
                .heightInches(heightInches)
                .formattedHeight(formattedHeight)
                .weightLbs(weightLbs)
                .bodyFatPercent(bodyFatPercent)
                .waistInches(waistInches)
                .chestInches(chestInches)
                .armInches(armInches)
                .assessmentDate(a.getAssessmentDate())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private int getInt(Map<String, Object> map, String key, Integer fallback) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return fallback != null ? fallback : 0;
    }

    private double getDouble(Map<String, Object> map, String key, Double fallback) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return fallback != null ? fallback : 0.0;
    }

    private Double getOptionalDouble(Map<String, Object> map, String key, Double fallback) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return fallback;
    }
}
