package com.mymentalcare.server.bootstrap.aichat.web.response

import java.time.LocalDateTime

data class AiChatMessageResponse(
    val messageId: Long,
    val segmentId: Long?,
    val senderType: String,
    val content: String,
    val messageOrder: Int,
    val isCrisisDetected: Boolean,
    val createdAt: LocalDateTime?,
)
