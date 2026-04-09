package com.campusfit.fitness.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_goal_adjustment_audit")
@EntityListeners(AuditingEntityListener.class)
public class GoalAdjustmentAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goal_id", nullable = false)
    private Long goalId;

    @Column(name = "previous_target", nullable = false, precision = 12, scale = 2)
    private BigDecimal previousTarget;

    @Column(name = "new_target", nullable = false, precision = 12, scale = 2)
    private BigDecimal newTarget;

    @Column(name = "previous_target_date", nullable = false)
    private LocalDate previousTargetDate;

    @Column(name = "new_target_date", nullable = false)
    private LocalDate newTargetDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "adjusted_by", nullable = false)
    private Long adjustedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
