package com.mymentalcare.server.application.aichat.response

data class StartAiChatSegmentResponse(
    val room: TodayAiChatRoomResponse,
    val segment: AiChatSegmentResponse,
    val checkIn: AiChatCheckInResponse?,
    val assistantMessage: AiChatMessageResponse,
    val crisisDetected: Boolean,
    val crisisGuideMessage: String?,
    val aiReplyFailed: Boolean = false,
    val aiReplyErrorMessage: String? = null,
)
