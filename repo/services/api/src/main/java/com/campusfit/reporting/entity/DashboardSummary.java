package com.campusfit.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_dashboard_summary")
public class DashboardSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_key", nullable = false, length = 100)
    private String metricKey;

    @Column(name = "metric_value", nullable = false, precision = 16, scale = 4)
    private BigDecimal metricValue;

    @Column(name = "dimension", length = 100)
    private String dimension;

    @Column(name = "period_type", nullable = false, length = 20)
    private String periodType;

    @Column(name = "period_value", nullable = false, length = 30)
    private String periodValue;

    @Column(name = "computed_at", nullable = false)
    @Builder.Default
    private LocalDateTime computedAt = LocalDateTime.now();
}
