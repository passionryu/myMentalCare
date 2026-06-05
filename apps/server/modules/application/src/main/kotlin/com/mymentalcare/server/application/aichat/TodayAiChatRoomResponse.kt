package com.mymentalcare.server.application.aichat

import java.time.LocalDate

data class TodayAiChatRoomResponse(
    val roomId: Long,
    val chatbotCode: String,
    val chatbotName: String,
    val conversationDate: LocalDate,
    val status: String,
    val messages: List<AiChatMessageResponse>,
)
