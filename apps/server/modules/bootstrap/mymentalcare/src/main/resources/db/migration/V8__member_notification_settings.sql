CREATE TABLE member_notification_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL,
    notification_time TIME NOT NULL,
    weekdays VARCHAR(40) NOT NULL,
    updated_at DATETIME NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_member_notification_settings_member UNIQUE (member_id),
    CONSTRAINT fk_member_notification_settings_member
        FOREIGN KEY (member_id) REFERENCES members (id)
);
