ALTER TABLE members
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER' AFTER status;

CREATE INDEX idx_members_role_status ON members (role, status);
