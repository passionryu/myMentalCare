package com.mymentalcare.server.application.aichat.request

import com.mymentalcare.server.application.aichat.response.*

data class SendAiChatMessageRequest(
    val content: String,
    val segmentId: Long? = null,
    val clientRequestId: String? = null,
)
