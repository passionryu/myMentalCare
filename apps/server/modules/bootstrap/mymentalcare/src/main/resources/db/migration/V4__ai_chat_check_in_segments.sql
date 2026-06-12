CREATE TABLE ai_chat_segments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    segment_order INT NOT NULL,
    start_type VARCHAR(40) NOT NULL,
    title VARCHAR(120) NOT NULL,
    client_request_id VARCHAR(80) NULL,
    started_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_ai_chat_segments_room_order UNIQUE (room_id, segment_order),
    CONSTRAINT uk_ai_chat_segments_room_client_request UNIQUE (room_id, client_request_id)
);

CREATE TABLE ai_chat_check_ins (
    id BIGINT NOT NULL AUTO_INCREMENT,
    segment_id BIGINT NOT NULL,
    template_type VARCHAR(40) NOT NULL,
    answers_json TEXT NOT NULL,
    summary_text VARCHAR(300) NOT NULL,
    is_crisis_detected BOOLEAN NOT NULL,
    detected_keywords VARCHAR(500) NULL,
    created_at DATETIME NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_ai_chat_check_ins_segment UNIQUE (segment_id)
);

ALTER TABLE chat_messages
    ADD COLUMN segment_id BIGINT NULL;

CREATE INDEX idx_chat_messages_room_segment_order
    ON chat_messages (room_id, segment_id, message_order);

ALTER TABLE crisis_detection_events
    ADD COLUMN source_type VARCHAR(30) NOT NULL DEFAULT 'MESSAGE',
    MODIFY message_id BIGINT NULL,
    ADD COLUMN check_in_id BIGINT NULL;
