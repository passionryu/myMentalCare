CREATE INDEX idx_members_created_at ON members (created_at);

CREATE INDEX idx_ai_chat_rooms_conversation_date ON ai_chat_rooms (conversation_date);

CREATE INDEX idx_ai_chat_reports_conversation_date ON ai_chat_reports (conversation_date);
