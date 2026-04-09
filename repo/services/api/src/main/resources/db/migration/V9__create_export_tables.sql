-- =====================================================
-- V9: Create export and deletion tables
-- =====================================================

CREATE TABLE t_export_job (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    user_id             BIGINT          NOT NULL,
    export_type         VARCHAR(30)     NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    file_path           VARCHAR(500)    NULL,
    password_protected  BOOLEAN         NOT NULL DEFAULT FALSE,
    expires_at          DATETIME        NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at        DATETIME        NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_export_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_export_user ON t_export_job (user_id);
CREATE INDEX idx_export_status ON t_export_job (status);
CREATE INDEX idx_export_expires ON t_export_job (expires_at);

-- -------------------------------------------------------

CREATE TABLE t_deletion_request (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    processed_at    DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_deletion_user FOREIGN KEY (user_id) REFERENCES t_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_deletion_user ON t_deletion_request (user_id);
CREATE INDEX idx_deletion_status ON t_deletion_request (status);
