-- =====================================================
-- V3: Create fitness module tables
-- =====================================================

CREATE TABLE t_assessment (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    user_id             BIGINT          NOT NULL,
    assessment_type     VARCHAR(20)     NOT NULL DEFAULT 'INITIAL',
    height_feet         INT             NOT NULL,
    height_inches       INT             NOT NULL,
    weight_lbs          DOUBLE          NOT NULL,
    body_fat_percent    DOUBLE          NULL,
    waist_inches        DOUBLE          NULL,
    chest_inches        DOUBLE          NULL,
    arm_inches          DOUBLE          NULL,
    metrics_encrypted   TEXT            NULL,
    assessment_date     DATE            NOT NULL,
    notes               TEXT            NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_assessment_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_assessment_user_id ON t_assessment (user_id);
CREATE INDEX idx_assessment_date ON t_assessment (user_id, assessment_date DESC);

-- -------------------------------------------------------

CREATE TABLE t_goal (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    user_id             BIGINT          NOT NULL,
    assessment_id       BIGINT          NULL,
    goal_type           VARCHAR(30)     NOT NULL,
    description         VARCHAR(500)    NULL,
    target_value        DECIMAL(12,2)   NOT NULL,
    start_value         DECIMAL(12,2)   NOT NULL,
    current_value       DECIMAL(12,2)   NOT NULL,
    unit                VARCHAR(30)     NOT NULL,
    start_date          DATE            NOT NULL,
    target_date         DATE            NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    missed_check_ins    INT             NOT NULL DEFAULT 0,
    version             INT             NOT NULL DEFAULT 0,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_goal_assessment FOREIGN KEY (assessment_id) REFERENCES t_assessment (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_goal_user_id ON t_goal (user_id);
CREATE INDEX idx_goal_user_status ON t_goal (user_id, status);

-- -------------------------------------------------------

CREATE TABLE t_milestone (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    goal_id             BIGINT          NOT NULL,
    description         VARCHAR(500)    NULL,
    target_value        DECIMAL(12,2)   NOT NULL,
    achieved_date       DATE            NULL,
    seq                 INT             NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_milestone_goal FOREIGN KEY (goal_id) REFERENCES t_goal (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_milestone_goal_id ON t_milestone (goal_id);

-- -------------------------------------------------------

CREATE TABLE t_check_in (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    goal_id             BIGINT          NOT NULL,
    user_id             BIGINT          NOT NULL,
    week_number         INT             NOT NULL,
    value               DECIMAL(12,2)   NOT NULL,
    notes               TEXT            NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_check_in_goal FOREIGN KEY (goal_id) REFERENCES t_goal (id) ON DELETE CASCADE,
    CONSTRAINT fk_check_in_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_check_in_goal_id ON t_check_in (goal_id);
CREATE INDEX idx_check_in_user_id ON t_check_in (user_id);

-- -------------------------------------------------------

CREATE TABLE t_goal_adjustment_audit (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    goal_id             BIGINT          NOT NULL,
    previous_target     DECIMAL(12,2)   NOT NULL,
    new_target          DECIMAL(12,2)   NOT NULL,
    previous_target_date DATE           NOT NULL,
    new_target_date     DATE            NOT NULL,
    reason              VARCHAR(500)    NULL,
    adjusted_by         BIGINT          NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_goal_adj_goal FOREIGN KEY (goal_id) REFERENCES t_goal (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_goal_adj_goal_id ON t_goal_adjustment_audit (goal_id);
