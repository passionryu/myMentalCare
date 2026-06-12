CREATE TABLE ai_chat_reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    conversation_date DATE NOT NULL,
    report_type VARCHAR(20) NOT NULL,
    summary TEXT NOT NULL,
    primary_emotion VARCHAR(80) NOT NULL,
    emotion_intensity INT NULL,
    main_cause VARCHAR(120) NOT NULL,
    emotional_flow TEXT NOT NULL,
    today_sentence VARCHAR(300) NOT NULL,
    client_request_id VARCHAR(80) NULL,
    created_at DATETIME NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_ai_chat_reports_room_client_request UNIQUE (room_id, client_request_id),
    INDEX idx_ai_chat_reports_member_date (member_id, conversation_date),
    INDEX idx_ai_chat_reports_room_created_at (room_id, created_at),
    CONSTRAINT fk_ai_chat_reports_room
        FOREIGN KEY (room_id) REFERENCES ai_chat_rooms (id)
);

CREATE TABLE ai_chat_report_songs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    song_order INT NOT NULL,
    title VARCHAR(120) NOT NULL,
    artist VARCHAR(120) NOT NULL,
    reason VARCHAR(300) NOT NULL,
    youtube_url VARCHAR(500) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_ai_chat_report_songs_report_order UNIQUE (report_id, song_order),
    CONSTRAINT fk_ai_chat_report_songs_report
        FOREIGN KEY (report_id) REFERENCES ai_chat_reports (id)
);
