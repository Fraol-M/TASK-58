-- Marker migration: the actual re-encryption of existing plaintext goal/check-in
-- metric values is handled by MetricsEncryptionBackfill.java at application startup.
-- This migration exists only to keep the Flyway version sequence contiguous.
SELECT 1;
