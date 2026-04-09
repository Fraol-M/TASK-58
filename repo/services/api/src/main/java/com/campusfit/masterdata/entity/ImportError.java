package com.campusfit.masterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_import_error")
public class ImportError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    @Column(name = "field", length = 100)
    private String field;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "raw_value", length = 500)
    private String rawValue;
}
