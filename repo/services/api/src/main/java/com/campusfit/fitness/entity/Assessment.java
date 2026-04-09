package com.campusfit.fitness.entity;

import com.campusfit.shared.encryption.AesEncryptor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_assessment")
@EntityListeners(AuditingEntityListener.class)
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "assessment_type", nullable = false, length = 20)
    @Builder.Default
    private String assessmentType = "INITIAL";

    // Plaintext columns kept for backward compatibility but no longer written to.
    // All sensitive metrics are stored in metricsEncrypted only.
    @Column(name = "height_feet")
    private Integer heightFeet;

    @Column(name = "height_inches")
    private Integer heightInches;

    @Column(name = "weight_lbs")
    private Double weightLbs;

    @Column(name = "body_fat_percent")
    private Double bodyFatPercent;

    @Column(name = "waist_inches")
    private Double waistInches;

    @Column(name = "chest_inches")
    private Double chestInches;

    @Column(name = "arm_inches")
    private Double armInches;

    @Convert(converter = AesEncryptor.class)
    @Column(name = "metrics_encrypted", nullable = false, columnDefinition = "TEXT")
    private String metricsEncrypted;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
