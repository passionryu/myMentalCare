CREATE TABLE social_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_user_id VARCHAR(120) NOT NULL,
    email VARCHAR(255) NULL,
    linked_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_social_accounts_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT uk_social_accounts_member_provider UNIQUE (member_id, provider),
    INDEX idx_social_accounts_member_id (member_id),
    INDEX idx_social_accounts_email (email),
    CONSTRAINT fk_social_accounts_member
        FOREIGN KEY (member_id) REFERENCES members (id)
);
