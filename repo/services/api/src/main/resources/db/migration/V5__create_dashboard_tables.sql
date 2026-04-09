-- =====================================================
-- V5: Create reporting / dashboard tables
-- =====================================================

CREATE TABLE t_dashboard_summary (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    metric_key      VARCHAR(100)    NOT NULL,
    metric_value    DECIMAL(16,4)   NOT NULL,
    dimension       VARCHAR(100)    NULL,
    period_type     VARCHAR(20)     NOT NULL,
    period_value    VARCHAR(30)     NOT NULL,
    computed_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_dashboard_key ON t_dashboard_summary (metric_key);
CREATE INDEX idx_dashboard_period ON t_dashboard_summary (period_type, period_value);
CREATE INDEX idx_dashboard_computed ON t_dashboard_summary (computed_at);
