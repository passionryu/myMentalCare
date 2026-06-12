package com.mymentalcare.server.application.aichat

data class SendAiChatMessageRequest(
    val content: String,
    val segmentId: Long? = null,
    val clientRequestId: String? = null,
)
