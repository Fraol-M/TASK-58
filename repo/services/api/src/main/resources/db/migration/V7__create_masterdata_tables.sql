-- =====================================================
-- V7: Create master data module tables
-- =====================================================

CREATE TABLE t_term (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    code            VARCHAR(50)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    effective_from  DATE            NOT NULL,
    effective_to    DATE            NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_term_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------

CREATE TABLE t_school (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    code            VARCHAR(50)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    effective_from  DATE            NOT NULL,
    effective_to    DATE            NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_school_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------

CREATE TABLE t_major (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    school_id       BIGINT          NOT NULL,
    code            VARCHAR(50)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    effective_from  DATE            NOT NULL,
    effective_to    DATE            NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_major_code (code),
    CONSTRAINT fk_major_school FOREIGN KEY (school_id) REFERENCES t_school (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_major_school ON t_major (school_id);

-- -------------------------------------------------------

CREATE TABLE t_academic_class (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    major_id        BIGINT          NOT NULL,
    code            VARCHAR(50)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    year            INT             NOT NULL,
    effective_from  DATE            NOT NULL,
    effective_to    DATE            NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_class_code (code),
    CONSTRAINT fk_class_major FOREIGN KEY (major_id) REFERENCES t_major (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_class_major ON t_academic_class (major_id);

-- -------------------------------------------------------

CREATE TABLE t_course (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    class_id        BIGINT          NOT NULL,
    term_id         BIGINT          NOT NULL,
    code            VARCHAR(50)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    credits         INT             NOT NULL DEFAULT 0,
    effective_from  DATE            NOT NULL,
    effective_to    DATE            NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_code (code),
    CONSTRAINT fk_course_class FOREIGN KEY (class_id) REFERENCES t_academic_class (id),
    CONSTRAINT fk_course_term FOREIGN KEY (term_id) REFERENCES t_term (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_course_class ON t_course (class_id);
CREATE INDEX idx_course_term ON t_course (term_id);

-- -------------------------------------------------------

CREATE TABLE t_import_job (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    file_name       VARCHAR(255)    NOT NULL,
    entity_type     VARCHAR(50)     NOT NULL,
    total_rows      INT             NOT NULL DEFAULT 0,
    success_count   INT             NOT NULL DEFAULT 0,
    error_count     INT             NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    uploaded_by     BIGINT          NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    DATETIME        NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_import_job_status ON t_import_job (status);

-- -------------------------------------------------------

CREATE TABLE t_import_error (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    job_id          BIGINT          NOT NULL,
    row_number      INT             NOT NULL,
    field           VARCHAR(100)    NULL,
    message         VARCHAR(500)    NOT NULL,
    raw_value       VARCHAR(500)    NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_import_err_job FOREIGN KEY (job_id) REFERENCES t_import_job (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_import_err_job ON t_import_error (job_id);

-- -------------------------------------------------------

CREATE TABLE t_merge_operation (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    entity_type     VARCHAR(50)     NOT NULL,
    source_id       BIGINT          NOT NULL,
    target_id       BIGINT          NOT NULL,
    merged_by       BIGINT          NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------

CREATE TABLE t_change_history (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    entity_type     VARCHAR(50)     NOT NULL,
    entity_id       BIGINT          NOT NULL,
    field           VARCHAR(100)    NOT NULL,
    old_value       TEXT            NULL,
    new_value       TEXT            NULL,
    changed_by      BIGINT          NOT NULL,
    changed_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_change_hist_entity ON t_change_history (entity_type, entity_id);
