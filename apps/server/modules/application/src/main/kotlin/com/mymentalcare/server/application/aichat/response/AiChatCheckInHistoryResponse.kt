package com.mymentalcare.server.application.aichat.response

import java.time.LocalDateTime

data class AiChatCheckInHistoryResponse(
    val checkInId: Long,
    val roomId: Long,
    val segmentId: Long,
    val templateType: String,
    val summaryText: String,
    val answers: List<AiChatCheckInAnswerResponse>,
    val createdAt: LocalDateTime?,
)

data class AiChatCheckInAnswerResponse(
    val stepKey: String,
    val optionKey: String?,
    val label: String?,
    val value: Int?,
    val freeText: String?,
)
