CREATE TABLE admin_incidents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(160) NOT NULL,
    description TEXT NULL,
    impact VARCHAR(30) NOT NULL,
    action TEXT NULL,
    created_by_member_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_admin_incidents_impact_created_at (impact, created_at),
    INDEX idx_admin_incidents_created_at (created_at)
);
