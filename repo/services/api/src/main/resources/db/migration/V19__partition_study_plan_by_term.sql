-- =====================================================
-- V19: Add term-based RANGE partitioning to t_study_plan
-- =====================================================
-- Context: V16 covered month-based partitioning on t_daily_completion and
-- t_forgetting_point.  V18 added composite term indexes.  This migration
-- completes the "partition by term and month" performance commitment by
-- adding explicit RANGE partitioning to t_study_plan on term_id so that
-- queries scoped to a term use partition pruning instead of a full table scan.
--
-- MySQL constraint: Partitioned InnoDB tables do not support foreign keys
-- (neither on the partitioned table itself nor from other tables that reference
-- it).  All FK constraints involving t_study_plan are dropped here; referential
-- integrity for these relationships is enforced at the service layer.

-- -------------------------------------------------------
-- Step 1: Drop FKs on child tables that reference t_study_plan
--         (MySQL disallows other tables from using FK references to a
--          partitioned table's primary key)
-- -------------------------------------------------------
ALTER TABLE t_study_plan_item   DROP FOREIGN KEY fk_study_item_plan;
ALTER TABLE t_daily_completion  DROP FOREIGN KEY fk_daily_comp_plan;
ALTER TABLE t_forgetting_point  DROP FOREIGN KEY fk_forget_plan;
ALTER TABLE t_streak            DROP FOREIGN KEY fk_streak_plan;

-- -------------------------------------------------------
-- Step 2: Drop the outgoing FK on t_study_plan itself
--         (a partitioned table may not carry FK constraints)
-- -------------------------------------------------------
ALTER TABLE t_study_plan DROP FOREIGN KEY fk_study_plan_user;

-- -------------------------------------------------------
-- Step 3: Make term_id NOT NULL
--         The partition column cannot be nullable; rows with no assigned term
--         receive term_id = 0 and land in the p_no_term partition.
-- -------------------------------------------------------
ALTER TABLE t_study_plan
    MODIFY COLUMN term_id BIGINT NOT NULL DEFAULT 0;

-- -------------------------------------------------------
-- Step 4: Rebuild the primary key as a composite (id, term_id)
--         MySQL requires every unique index — including the PK — to include
--         all partition columns.
-- -------------------------------------------------------
ALTER TABLE t_study_plan
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (id, term_id);

-- -------------------------------------------------------
-- Step 5: Apply RANGE partitioning on term_id
--         Boundaries use sequential ID buckets that map to academic-term IDs
--         seeded by master-data imports.  New buckets are added via
--         ALTER TABLE t_study_plan ADD PARTITION as additional terms are created.
-- -------------------------------------------------------
ALTER TABLE t_study_plan
    PARTITION BY RANGE (term_id) (
        PARTITION p_no_term     VALUES LESS THAN (1),    -- term_id = 0  (no term assigned)
        PARTITION p_term_1_10   VALUES LESS THAN (11),   -- terms  1 – 10
        PARTITION p_term_11_20  VALUES LESS THAN (21),   -- terms 11 – 20
        PARTITION p_term_21_30  VALUES LESS THAN (31),   -- terms 21 – 30
        PARTITION p_term_31_50  VALUES LESS THAN (51),   -- terms 31 – 50
        PARTITION p_term_51_100 VALUES LESS THAN (101),  -- terms 51 – 100
        PARTITION p_future      VALUES LESS THAN MAXVALUE
    );
