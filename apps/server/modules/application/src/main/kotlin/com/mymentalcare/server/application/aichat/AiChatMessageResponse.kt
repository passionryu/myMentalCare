package com.mymentalcare.server.application.aichat

import java.time.LocalDateTime

data class AiChatMessageResponse(
    val messageId: Long,
    val senderType: String,
    val content: String,
    val messageOrder: Int,
    val isCrisisDetected: Boolean,
    val createdAt: LocalDateTime?,
)
