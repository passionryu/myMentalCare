package com.mymentalcare.server.application.aichat

import java.time.LocalDate

data class TodayAiChatRoomResponse(
    val roomId: Long,
    val chatbotCode: String,
    val chatbotName: String,
    val conversationDate: LocalDate,
    val status: String,
    val hasConversation: Boolean,
    val activeSegmentId: Long?,
    val segments: List<AiChatSegmentResponse>,
    val messages: List<AiChatMessageResponse>,
)

data class AiChatSegmentResponse(
    val segmentId: Long,
    val segmentOrder: Int,
    val startType: String,
    val title: String,
    val startedAt: java.time.LocalDateTime?,
    val checkIn: AiChatCheckInResponse?,
)

data class AiChatCheckInResponse(
    val checkInId: Long,
    val templateType: String,
    val summaryText: String,
)
