-- =====================================================
-- V1: Create auth and audit tables
-- =====================================================

CREATE TABLE t_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)     NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    email           VARCHAR(100)    NULL,
    phone           VARCHAR(30)     NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    failed_attempts INT             NOT NULL DEFAULT 0,
    lockout_until   DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_status ON t_user (status);
CREATE INDEX idx_user_email ON t_user (email);

-- -------------------------------------------------------

CREATE TABLE t_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    code            VARCHAR(50)     NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------

CREATE TABLE t_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    code            VARCHAR(100)    NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    resource        VARCHAR(50)     NOT NULL,
    action          VARCHAR(50)     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_permission_resource ON t_permission (resource);

-- -------------------------------------------------------

CREATE TABLE t_user_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    role_id         BIGINT          NOT NULL,
    assigned_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by     BIGINT          NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES t_role (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_role_user_id ON t_user_role (user_id);
CREATE INDEX idx_user_role_role_id ON t_user_role (role_id);

-- -------------------------------------------------------

CREATE TABLE t_role_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    role_id         BIGINT          NOT NULL,
    permission_id   BIGINT          NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES t_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) REFERENCES t_permission (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_role_perm_role_id ON t_role_permission (role_id);
CREATE INDEX idx_role_perm_perm_id ON t_role_permission (permission_id);

-- -------------------------------------------------------

CREATE TABLE t_session (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    token           VARCHAR(255)    NOT NULL,
    ip_address      VARCHAR(45)     NULL,
    user_agent      VARCHAR(500)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      DATETIME        NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_session_token (token),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_session_user_id ON t_session (user_id);
CREATE INDEX idx_session_expires_at ON t_session (expires_at);

-- -------------------------------------------------------

CREATE TABLE t_audit_log (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    entity_type     VARCHAR(100)    NOT NULL,
    entity_id       BIGINT          NOT NULL,
    action          VARCHAR(50)     NOT NULL,
    old_value       TEXT            NULL,
    new_value       TEXT            NULL,
    user_id         BIGINT          NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_entity ON t_audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_user_id ON t_audit_log (user_id);
CREATE INDEX idx_audit_created_at ON t_audit_log (created_at);
