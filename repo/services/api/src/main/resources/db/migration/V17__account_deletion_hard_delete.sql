-- =====================================================
-- V17: Support hard-delete of user row on account deletion
-- Change non-cascading FK references to SET NULL
-- so the user row can be fully removed while
-- preserving de-identified audit/operational records.
-- =====================================================

-- t_inbound_receipt.created_by: allow NULL, ON DELETE SET NULL
ALTER TABLE t_inbound_receipt MODIFY COLUMN created_by BIGINT NULL;
ALTER TABLE t_inbound_receipt DROP FOREIGN KEY fk_receipt_created_by;
ALTER TABLE t_inbound_receipt ADD CONSTRAINT fk_receipt_created_by
    FOREIGN KEY (created_by) REFERENCES t_user (id) ON DELETE SET NULL;

-- t_notification.created_by: allow NULL, ON DELETE SET NULL
ALTER TABLE t_notification MODIFY COLUMN created_by BIGINT NULL;
ALTER TABLE t_notification DROP FOREIGN KEY fk_notification_creator;
ALTER TABLE t_notification ADD CONSTRAINT fk_notification_creator
    FOREIGN KEY (created_by) REFERENCES t_user (id) ON DELETE SET NULL;

-- t_deletion_request.user_id: allow NULL, ON DELETE SET NULL
-- The deletion_request record itself is the de-identified audit artifact
ALTER TABLE t_deletion_request MODIFY COLUMN user_id BIGINT NULL;
ALTER TABLE t_deletion_request DROP FOREIGN KEY fk_deletion_user;
ALTER TABLE t_deletion_request ADD CONSTRAINT fk_deletion_user
    FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE SET NULL;
