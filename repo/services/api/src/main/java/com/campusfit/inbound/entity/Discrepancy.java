package com.campusfit.inbound.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_discrepancy")
@EntityListeners(AuditingEntityListener.class)
public class Discrepancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_id", nullable = false)
    private Long receiptId;

    @Column(name = "line_id", nullable = false)
    private Long lineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "discrepancy_type", nullable = false, length = 20)
    private DiscrepancyType discrepancyType;

    @Column(name = "expected_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal expectedValue;

    @Column(name = "actual_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal actualValue;

    @Column(name = "variance_percent", nullable = false, precision = 8, scale = 2)
    private BigDecimal variancePercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", length = 30)
    private ReasonCode reasonCode;

    @Column(name = "supervisor_required", nullable = false)
    @Builder.Default
    private boolean supervisorRequired = false;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum DiscrepancyType {
        QUANTITY, QUALITY, WRONG_ITEM
    }

    public enum ReasonCode {
        DAMAGED, SHORT_SHIP, OVER_SHIP, WRONG_ITEM, QUALITY_FAIL, OTHER
    }
}
