CREATE TABLE inquiries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    category VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_inquiries_member_created_at (member_id, created_at),
    CONSTRAINT fk_inquiries_member
        FOREIGN KEY (member_id) REFERENCES members (id)
);
