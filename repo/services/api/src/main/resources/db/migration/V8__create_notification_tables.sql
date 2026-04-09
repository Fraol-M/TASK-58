-- =====================================================
-- V8: Create notification module tables
-- =====================================================

CREATE TABLE t_notification (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    type            VARCHAR(30)     NOT NULL,
    title           VARCHAR(255)    NOT NULL,
    body            TEXT            NULL,
    created_by      BIGINT          NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_creator FOREIGN KEY (created_by) REFERENCES t_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notification_type ON t_notification (type);

-- -------------------------------------------------------

CREATE TABLE t_notification_target (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    notification_id BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    read_at         DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_target_notification FOREIGN KEY (notification_id) REFERENCES t_notification (id) ON DELETE CASCADE,
    CONSTRAINT fk_target_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_target_notification ON t_notification_target (notification_id);
CREATE INDEX idx_target_user ON t_notification_target (user_id);
CREATE INDEX idx_target_user_read ON t_notification_target (user_id, read_at);

-- -------------------------------------------------------

CREATE TABLE t_notification_delivery (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    target_id       BIGINT          NOT NULL,
    channel         VARCHAR(20)     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    sent_at         DATETIME        NULL,
    failure_reason  VARCHAR(500)    NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_delivery_target FOREIGN KEY (target_id) REFERENCES t_notification_target (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_delivery_target ON t_notification_delivery (target_id);
CREATE INDEX idx_delivery_status ON t_notification_delivery (status);
