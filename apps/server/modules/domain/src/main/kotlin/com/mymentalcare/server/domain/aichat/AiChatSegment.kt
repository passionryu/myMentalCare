package com.mymentalcare.server.domain.aichat

import java.time.LocalDateTime

data class AiChatSegment(
    val id: Long,
    val roomId: Long,
    val segmentOrder: Int,
    val startType: AiChatSegmentStartType,
    val title: String,
    val clientRequestId: String?,
    val startedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

enum class AiChatSegmentStartType {
    DIRECT,
    BASIC_EMOTION,
    CONVERSATION_START,
    CONDITION,
    DAY_REVIEW,
}
