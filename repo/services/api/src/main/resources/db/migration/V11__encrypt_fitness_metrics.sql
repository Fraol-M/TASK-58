-- Make plaintext metric columns nullable since data is now stored encrypted
ALTER TABLE t_assessment MODIFY COLUMN height_feet INT NULL;
ALTER TABLE t_assessment MODIFY COLUMN height_inches INT NULL;
ALTER TABLE t_assessment MODIFY COLUMN weight_lbs DOUBLE NULL;
ALTER TABLE t_assessment MODIFY COLUMN metrics_encrypted TEXT NOT NULL;

-- Backfill: for existing rows that have plaintext but no encrypted data,
-- the application will need to re-encrypt on first read. Mark them.
UPDATE t_assessment SET metrics_encrypted = 'PENDING_MIGRATION' WHERE metrics_encrypted IS NULL;
