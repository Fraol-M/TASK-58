package com.campusfit.fitness.entity;

import com.campusfit.shared.encryption.AesEncryptor;
import com.campusfit.shared.encryption.BigDecimalAesEncryptor;
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
@Table(name = "t_check_in")
@EntityListeners(AuditingEntityListener.class)
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goal_id", nullable = false)
    private Long goalId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @Convert(converter = BigDecimalAesEncryptor.class)
    @Column(name = "`value`", nullable = false, columnDefinition = "TEXT")
    private BigDecimal value;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Convert(converter = AesEncryptor.class)
    @Column(name = "value_encrypted", columnDefinition = "TEXT")
    private String valueEncrypted;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
