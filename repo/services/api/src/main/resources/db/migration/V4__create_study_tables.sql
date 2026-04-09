-- =====================================================
-- V4: Create study module tables
-- =====================================================

CREATE TABLE t_study_plan (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    course_id       BIGINT          NULL,
    term_id         BIGINT          NULL,
    title           VARCHAR(255)    NOT NULL,
    description     TEXT            NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_study_plan_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_study_plan_user_id ON t_study_plan (user_id);
CREATE INDEX idx_study_plan_user_status ON t_study_plan (user_id, status);

-- -------------------------------------------------------

CREATE TABLE t_study_plan_item (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    plan_id         BIGINT          NOT NULL,
    title           VARCHAR(255)    NOT NULL,
    description     TEXT            NULL,
    due_date        DATE            NULL,
    seq             INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_study_item_plan FOREIGN KEY (plan_id) REFERENCES t_study_plan (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_study_item_plan_id ON t_study_plan_item (plan_id);

-- -------------------------------------------------------

CREATE TABLE t_daily_completion (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    plan_id         BIGINT          NOT NULL,
    item_id         BIGINT          NULL,
    completed_date  DATE            NOT NULL,
    completed       BOOLEAN         NOT NULL DEFAULT FALSE,
    notes           TEXT            NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_daily_comp_plan FOREIGN KEY (plan_id) REFERENCES t_study_plan (id) ON DELETE CASCADE,
    CONSTRAINT fk_daily_comp_item FOREIGN KEY (item_id) REFERENCES t_study_plan_item (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_daily_comp_plan_id ON t_daily_completion (plan_id);
CREATE INDEX idx_daily_comp_date ON t_daily_completion (plan_id, completed_date);

-- -------------------------------------------------------

CREATE TABLE t_forgetting_point (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    plan_id         BIGINT          NOT NULL,
    topic           VARCHAR(255)    NOT NULL,
    description     TEXT            NULL,
    next_review_date DATE           NOT NULL,
    ease_factor     DECIMAL(5,2)    NOT NULL DEFAULT 2.50,
    interval_days   INT             NOT NULL DEFAULT 1,
    repetitions     INT             NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_forget_plan FOREIGN KEY (plan_id) REFERENCES t_study_plan (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_forget_plan_id ON t_forgetting_point (plan_id);
CREATE INDEX idx_forget_review_date ON t_forgetting_point (next_review_date);

-- -------------------------------------------------------

CREATE TABLE t_streak (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    plan_id         BIGINT          NOT NULL,
    current_streak  INT             NOT NULL DEFAULT 0,
    longest_streak  INT             NOT NULL DEFAULT 0,
    last_active_date DATE           NULL,
    updated_at      DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_streak_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_streak_plan FOREIGN KEY (plan_id) REFERENCES t_study_plan (id) ON DELETE CASCADE,
    UNIQUE KEY uk_streak_user_plan (user_id, plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_streak_user_id ON t_streak (user_id);
