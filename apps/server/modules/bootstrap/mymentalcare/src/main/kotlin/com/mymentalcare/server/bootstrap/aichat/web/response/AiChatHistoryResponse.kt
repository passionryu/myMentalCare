package com.mymentalcare.server.bootstrap.aichat.web.response

import java.time.LocalDate
import java.time.LocalDateTime

data class AiChatHistoryRoomResponse(
    val roomId: Long,
    val conversationDate: LocalDate,
    val status: String,
    val messageCount: Int,
    val latestMessage: String?,
    val latestMessageAt: LocalDateTime?,
)

data class AiChatHistoryRoomDetailResponse(
    val roomId: Long,
    val chatbotCode: String,
    val chatbotName: String,
    val conversationDate: LocalDate,
    val status: String,
    val messages: List<AiChatMessageResponse>,
)
