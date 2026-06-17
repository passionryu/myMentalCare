package com.mymentalcare.server.bootstrap.aichat.web.response

data class SendAiChatMessageResponse(
    val room: TodayAiChatRoomResponse,
    val userMessage: AiChatMessageResponse,
    val assistantMessage: AiChatMessageResponse,
    val crisisDetected: Boolean,
    val crisisGuideMessage: String?,
    val aiReplyFailed: Boolean,
    val aiReplyErrorMessage: String?,
)
