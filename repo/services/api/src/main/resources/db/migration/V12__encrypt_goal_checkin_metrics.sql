-- Add encrypted metrics columns to goals and check-ins
ALTER TABLE t_goal ADD COLUMN metrics_encrypted TEXT NULL;
ALTER TABLE t_check_in ADD COLUMN value_encrypted TEXT NULL;
