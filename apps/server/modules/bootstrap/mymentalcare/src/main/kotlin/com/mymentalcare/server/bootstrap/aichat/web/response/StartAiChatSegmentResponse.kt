package com.mymentalcare.server.bootstrap.aichat.web.response

data class StartAiChatSegmentResponse(
    val room: TodayAiChatRoomResponse,
    val segment: AiChatSegmentResponse,
    val checkIn: AiChatCheckInResponse?,
    val assistantMessage: AiChatMessageResponse,
    val crisisDetected: Boolean,
    val crisisGuideMessage: String?,
    val aiReplyFailed: Boolean,
    val aiReplyErrorMessage: String?,
)
