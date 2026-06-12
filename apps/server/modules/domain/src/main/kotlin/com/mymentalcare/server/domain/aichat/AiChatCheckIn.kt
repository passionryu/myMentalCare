package com.mymentalcare.server.domain.aichat

import java.time.LocalDateTime

data class AiChatCheckIn(
    val id: Long,
    val segmentId: Long,
    val templateType: AiChatCheckInTemplateType,
    val answers: List<AiChatCheckInAnswer>,
    val summaryText: String,
    val isCrisisDetected: Boolean,
    val detectedKeywords: List<String>,
    val createdAt: LocalDateTime? = null,
)

data class AiChatCheckInAnswer(
    val stepKey: String,
    val optionKey: String? = null,
    val label: String? = null,
    val value: Int? = null,
    val freeText: String? = null,
)

enum class AiChatCheckInTemplateType {
    BASIC_EMOTION,
    CONVERSATION_START,
    CONDITION,
    DAY_REVIEW,
}
