ALTER TABLE inquiries
    ADD COLUMN admin_memo TEXT NULL AFTER status,
    ADD COLUMN handled_by_member_id BIGINT NULL AFTER admin_memo,
    ADD COLUMN handled_at DATETIME NULL AFTER handled_by_member_id,
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER created_at;

CREATE INDEX idx_inquiries_status_created_at ON inquiries (status, created_at);
CREATE INDEX idx_inquiries_member_id ON inquiries (member_id);
