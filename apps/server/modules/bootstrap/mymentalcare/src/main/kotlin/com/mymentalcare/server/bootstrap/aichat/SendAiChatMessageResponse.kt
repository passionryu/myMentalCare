package com.mymentalcare.server.bootstrap.aichat

data class SendAiChatMessageResponse(
    val room: TodayAiChatRoomResponse,
    val userMessage: AiChatMessageResponse,
    val assistantMessage: AiChatMessageResponse,
    val crisisDetected: Boolean,
    val crisisGuideMessage: String?,
)
