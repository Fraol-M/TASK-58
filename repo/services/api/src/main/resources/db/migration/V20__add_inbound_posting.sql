-- =====================================================
-- V20: Add posting/unposting tracking columns to inbound receipt
-- Adds POSTED and UNPOSTED to the receipt lifecycle state machine.
-- =====================================================

ALTER TABLE t_inbound_receipt
    ADD COLUMN posted_by   BIGINT   NULL,
    ADD COLUMN posted_at   DATETIME NULL,
    ADD COLUMN unposted_by BIGINT   NULL,
    ADD COLUMN unposted_at DATETIME NULL;
