ALTER TABLE ai_chat_reports
    ADD COLUMN emotion_score INT NULL AFTER emotion_intensity;

CREATE TABLE ai_chat_report_emotion_points (
    id BIGINT NOT NULL AUTO_INCREMENT,
    report_id BIGINT NOT NULL,
    point_order INT NOT NULL,
    message_order INT NULL,
    label VARCHAR(80) NOT NULL,
    score INT NOT NULL,
    reason VARCHAR(240) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_ai_chat_report_emotion_points_report_order UNIQUE (report_id, point_order),
    INDEX idx_ai_chat_report_emotion_points_report (report_id),
    CONSTRAINT fk_ai_chat_report_emotion_points_report
        FOREIGN KEY (report_id) REFERENCES ai_chat_reports (id)
);
