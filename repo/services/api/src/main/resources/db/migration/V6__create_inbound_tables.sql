-- =====================================================
-- V6: Create inbound module tables
-- =====================================================

CREATE TABLE t_inbound_receipt (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    receipt_number              VARCHAR(50)     NOT NULL,
    receipt_type                VARCHAR(20)     NOT NULL,
    reference_number            VARCHAR(100)    NULL,
    supplier_name               VARCHAR(255)    NULL,
    status                      VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    expected_date               DATE            NULL,
    received_date               DATE            NULL,
    created_by                  BIGINT          NOT NULL,
    supervisor_approval_required BOOLEAN        NOT NULL DEFAULT FALSE,
    supervisor_approved_by      BIGINT          NULL,
    version                     INT             NOT NULL DEFAULT 0,
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_receipt_number (receipt_number),
    CONSTRAINT fk_receipt_created_by FOREIGN KEY (created_by) REFERENCES t_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_receipt_status ON t_inbound_receipt (status);
CREATE INDEX idx_receipt_created_by ON t_inbound_receipt (created_by);

-- -------------------------------------------------------

CREATE TABLE t_inbound_line (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    receipt_id          BIGINT          NOT NULL,
    item_code           VARCHAR(50)     NOT NULL,
    item_name           VARCHAR(255)    NOT NULL,
    expected_qty        DECIMAL(12,2)   NOT NULL,
    received_qty        DECIMAL(12,2)   NOT NULL DEFAULT 0,
    inspected_qty       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    unit_cost           DECIMAL(12,2)   NOT NULL DEFAULT 0,
    inspection_result   VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    inspection_notes    TEXT            NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_line_receipt FOREIGN KEY (receipt_id) REFERENCES t_inbound_receipt (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_line_receipt_id ON t_inbound_line (receipt_id);

-- -------------------------------------------------------

CREATE TABLE t_inbound_state_history (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    receipt_id      BIGINT          NOT NULL,
    from_state      VARCHAR(20)     NULL,
    to_state        VARCHAR(20)     NOT NULL,
    changed_by      BIGINT          NOT NULL,
    reason          VARCHAR(500)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_state_hist_receipt FOREIGN KEY (receipt_id) REFERENCES t_inbound_receipt (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_state_hist_receipt ON t_inbound_state_history (receipt_id);

-- -------------------------------------------------------

CREATE TABLE t_discrepancy (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    receipt_id          BIGINT          NOT NULL,
    line_id             BIGINT          NOT NULL,
    discrepancy_type    VARCHAR(20)     NOT NULL,
    expected_value      DECIMAL(12,2)   NOT NULL,
    actual_value        DECIMAL(12,2)   NOT NULL,
    variance_percent    DECIMAL(8,2)    NOT NULL,
    reason_code         VARCHAR(30)     NULL,
    supervisor_required BOOLEAN         NOT NULL DEFAULT FALSE,
    resolved_by         BIGINT          NULL,
    resolved_at         DATETIME        NULL,
    notes               TEXT            NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_discrep_receipt FOREIGN KEY (receipt_id) REFERENCES t_inbound_receipt (id) ON DELETE CASCADE,
    CONSTRAINT fk_discrep_line FOREIGN KEY (line_id) REFERENCES t_inbound_line (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_discrep_receipt ON t_discrepancy (receipt_id);
CREATE INDEX idx_discrep_line ON t_discrepancy (line_id);

-- -------------------------------------------------------

CREATE TABLE t_putaway_task (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    receipt_id          BIGINT          NOT NULL,
    line_id             BIGINT          NOT NULL,
    suggested_location  VARCHAR(100)    NULL,
    actual_location     VARCHAR(100)    NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    completed_by        BIGINT          NULL,
    completed_at        DATETIME        NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_putaway_receipt FOREIGN KEY (receipt_id) REFERENCES t_inbound_receipt (id) ON DELETE CASCADE,
    CONSTRAINT fk_putaway_line FOREIGN KEY (line_id) REFERENCES t_inbound_line (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_putaway_receipt ON t_putaway_task (receipt_id);
CREATE INDEX idx_putaway_status ON t_putaway_task (status);
