package com.mymentalcare.server.domain.aichat

import java.time.LocalDateTime

data class AiChatRoomSummary(
    val id: Long,
    val roomId: Long,
    val memberId: Long,
    val lastSummarizedMessageId: Long?,
    val summary: String,
    val emotionalState: String?,
    val activeTopics: String?,
    val unresolvedQuestions: String?,
    val userPreferences: String?,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
