package com.campusfit.fitness.entity;

import com.campusfit.shared.encryption.AesEncryptor;
import com.campusfit.shared.encryption.BigDecimalAesEncryptor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_goal")
@EntityListeners(AuditingEntityListener.class)
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "assessment_id")
    private Long assessmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 30)
    private GoalType goalType;

    @Column(name = "description", length = 500)
    private String description;

    @Convert(converter = BigDecimalAesEncryptor.class)
    @Column(name = "target_value", nullable = false, columnDefinition = "TEXT")
    private BigDecimal targetValue;

    @Convert(converter = BigDecimalAesEncryptor.class)
    @Column(name = "start_value", nullable = false, columnDefinition = "TEXT")
    private BigDecimal startValue;

    @Convert(converter = BigDecimalAesEncryptor.class)
    @Column(name = "current_value", nullable = false, columnDefinition = "TEXT")
    private BigDecimal currentValue;

    @Column(name = "unit", nullable = false, length = 30)
    private String unit;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;

    @Column(name = "missed_check_ins", nullable = false)
    @Builder.Default
    private int missedCheckIns = 0;

    @Convert(converter = AesEncryptor.class)
    @Column(name = "metrics_encrypted", columnDefinition = "TEXT")
    private String metricsEncrypted;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private int version = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum GoalType {
        WEIGHT_LOSS, WEIGHT_GAIN, FLEXIBILITY, ENDURANCE, STRENGTH
    }

    public enum GoalStatus {
        ACTIVE, ACHIEVED, RECALCULATED, ABANDONED
    }
}
