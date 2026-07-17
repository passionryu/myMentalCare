package com.mymentalcare.server.domain.admin

enum class AdminAuditLogAction {
    ADMIN_CONSOLE_ACCESS,
    AUDIT_LOG_VIEW,
    MEMBER_VIEW,
    MEMBER_STATUS_CHANGE,
    INQUIRY_VIEW,
    INQUIRY_STATUS_CHANGE,
    INQUIRY_MEMO_CHANGE,
    AI_CHAT_HISTORY_VIEW,
    AI_CHAT_REPORT_VIEW,
    SYSTEM_LOG_VIEW,
}
