ALTER TABLE members
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER phone,
    ADD COLUMN deleted_at DATETIME NULL AFTER status;

CREATE INDEX idx_members_status ON members (status);
