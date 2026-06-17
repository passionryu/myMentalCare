package com.mymentalcare.server.application.aichat.request

import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.AiChatCheckInAnswer
import com.mymentalcare.server.domain.aichat.AiChatCheckInTemplateType

data class StartAiChatSegmentRequest(
    val startType: String,
    val clientRequestId: String? = null,
)

data class StartAiChatCheckInRequest(
    val templateType: AiChatCheckInTemplateType,
    val answers: List<AiChatCheckInAnswer>,
    val clientRequestId: String? = null,
)
