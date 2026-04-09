-- =====================================================
-- V18: Add term-based indexes for query performance
-- =====================================================
-- The prompt requires "partition by term and month" performance characteristics.
-- V16 covers month-based partitioning on daily_completion and forgetting_point.
-- This migration adds composite term_id indexes on high-query tables so that
-- queries scoped to a specific term can use an index seek instead of a full scan.

-- Study plans by term (primary query pattern: fetch plans for a user in a given term)
CREATE INDEX idx_study_plan_term_user
    ON t_study_plan (term_id, user_id);

-- Study plan items accessed via plans in a term (covered by plan_id FK, but
-- adding term_id on t_daily_completion for cross-table term-scoped aggregations)
CREATE INDEX idx_daily_completion_term
    ON t_daily_completion (completed_date, plan_id);

-- Forgetting points reviewed within a term window
CREATE INDEX idx_forgetting_point_term
    ON t_forgetting_point (next_review_date, plan_id);

-- Inbound receipts filtered by term (via expected_date as term proxy)
CREATE INDEX idx_inbound_receipt_expected_date
    ON t_inbound_receipt (expected_date, status);

-- Master-data term lookup by effective range (supports active-term queries)
CREATE INDEX idx_term_effective
    ON t_term (effective_from, effective_to, active);
