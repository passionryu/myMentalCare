CREATE TABLE admin_audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admin_member_id BIGINT NOT NULL,
    admin_login_id VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NULL,
    target_id BIGINT NULL,
    reason TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_admin_audit_logs_created_at (created_at, id),
    INDEX idx_admin_audit_logs_target (target_type, target_id),
    INDEX idx_admin_audit_logs_admin (admin_member_id, created_at)
);
