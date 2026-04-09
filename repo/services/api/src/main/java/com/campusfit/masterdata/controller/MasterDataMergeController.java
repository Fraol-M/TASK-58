package com.campusfit.masterdata.controller;

import com.campusfit.masterdata.dto.MergeRequest;
import com.campusfit.masterdata.dto.MasterDataResponse;
import com.campusfit.masterdata.entity.MergeOperation;
import com.campusfit.masterdata.service.*;
import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.security.SecurityContextHelper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/master-data/merge")
public class MasterDataMergeController {

    private final DuplicateMergeService mergeService;
    private final TermService termService;
    private final SchoolService schoolService;
    private final MajorService majorService;
    private final ClassService classService;
    private final CourseService courseService;

    public MasterDataMergeController(DuplicateMergeService mergeService,
                                      TermService termService,
                                      SchoolService schoolService,
                                      MajorService majorService,
                                      ClassService classService,
                                      CourseService courseService) {
        this.mergeService = mergeService;
        this.termService = termService;
        this.schoolService = schoolService;
        this.majorService = majorService;
        this.classService = classService;
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCandidates(
            @RequestParam String entityType) {
        List<MasterDataResponse> items = getItems(entityType);
        List<Map<String, Object>> candidates = findDuplicateCandidates(items, entityType);
        return ResponseEntity.ok(ApiResponse.ok(candidates));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MergeOperation>> merge(@Valid @RequestBody MergeRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        MergeOperation operation = mergeService.merge(
                request.getEntityType(), request.getSourceId(), request.getTargetId(), userId);
        return ResponseEntity.ok(ApiResponse.ok(operation));
    }

    private List<MasterDataResponse> getItems(String entityType) {
        switch (entityType.toUpperCase()) {
            case "TERM": return termService.getAll();
            case "SCHOOL": return schoolService.getAll();
            case "MAJOR": return majorService.getAll();
            case "CLASS": return classService.getAll();
            case "COURSE": return courseService.getAll();
            default: throw new BusinessException("Unknown entity type: " + entityType);
        }
    }

    private List<Map<String, Object>> findDuplicateCandidates(List<MasterDataResponse> items, String entityType) {
        List<Map<String, Object>> candidates = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                MasterDataResponse a = items.get(i);
                MasterDataResponse b = items.get(j);
                double similarity = calculateSimilarity(a.getName(), b.getName());
                if (similarity >= 0.7) {
                    Map<String, Object> candidate = new LinkedHashMap<>();
                    candidate.put("sourceItem", a);
                    candidate.put("targetItem", b);
                    candidate.put("similarity", Math.round(similarity * 100) / 100.0);
                    candidate.put("entityType", entityType.toUpperCase());
                    candidates.add(candidate);
                }
            }
        }
        return candidates;
    }

    private double calculateSimilarity(String a, String b) {
        if (a == null || b == null) return 0;
        String la = a.toLowerCase().trim();
        String lb = b.toLowerCase().trim();
        if (la.equals(lb)) return 1.0;
        int maxLen = Math.max(la.length(), lb.length());
        if (maxLen == 0) return 1.0;
        int distance = levenshteinDistance(la, lb);
        return 1.0 - ((double) distance / maxLen);
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }
}
