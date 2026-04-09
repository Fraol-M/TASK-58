-- Add last_accessed_at column for inactivity-based session timeout
ALTER TABLE t_session ADD COLUMN last_accessed_at DATETIME NULL;

-- Backfill existing sessions with created_at as last accessed time
UPDATE t_session SET last_accessed_at = created_at WHERE last_accessed_at IS NULL;
