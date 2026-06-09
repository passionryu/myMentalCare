CREATE TABLE ai_chat_room_summaries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    last_summarized_message_id BIGINT NULL,
    summary TEXT NOT NULL,
    emotional_state VARCHAR(500) NULL,
    active_topics TEXT NULL,
    unresolved_questions TEXT NULL,
    user_preferences TEXT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_ai_chat_room_summaries_room UNIQUE (room_id),
    INDEX idx_ai_chat_room_summaries_member_room (member_id, room_id),
    CONSTRAINT fk_ai_chat_room_summaries_room
        FOREIGN KEY (room_id) REFERENCES ai_chat_rooms (id),
    CONSTRAINT fk_ai_chat_room_summaries_last_message
        FOREIGN KEY (last_summarized_message_id) REFERENCES chat_messages (id)
);
