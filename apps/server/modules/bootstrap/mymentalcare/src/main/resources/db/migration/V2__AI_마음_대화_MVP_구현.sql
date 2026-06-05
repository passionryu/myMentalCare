CREATE TABLE ai_chat_rooms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    chatbot_code VARCHAR(50) NOT NULL,
    conversation_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_ai_chat_rooms_member_chatbot_date UNIQUE (member_id, chatbot_code, conversation_date)
);

CREATE TABLE chat_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    sender_type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    message_order INT NOT NULL,
    is_crisis_detected BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_chat_messages_room_order UNIQUE (room_id, message_order)
);

CREATE TABLE crisis_detection_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    message_id BIGINT NOT NULL,
    detected_keywords VARCHAR(500) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    handled_action VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,

    PRIMARY KEY (id)
);
