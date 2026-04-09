package com.campusfit.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private String userRole;
    private List<MetricEntry> metrics;
    private Map<String, Object> summary;
    private List<ActivityEntry> recentActivity;
    private Map<String, List<Number>> charts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityEntry {
        private String label;
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricEntry {
        private String key;
        private BigDecimal value;
        private String dimension;
        private String periodType;
        private String periodValue;
    }
}
