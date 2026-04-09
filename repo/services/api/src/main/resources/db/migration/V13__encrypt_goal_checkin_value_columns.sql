-- Migrate goal metric columns from DECIMAL to TEXT to store AES-encrypted values.
-- Existing plaintext values are preserved during migration; the application will
-- encrypt them on next write via the BigDecimalAesEncryptor JPA converter.

ALTER TABLE t_goal MODIFY COLUMN target_value TEXT NOT NULL;
ALTER TABLE t_goal MODIFY COLUMN start_value TEXT NOT NULL;
ALTER TABLE t_goal MODIFY COLUMN current_value TEXT NOT NULL;

-- Migrate check-in value column from DECIMAL to TEXT for encrypted storage.
ALTER TABLE t_check_in MODIFY COLUMN value TEXT NOT NULL;
