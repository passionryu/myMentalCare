package com.mymentalcare.server.domain.aichat

import java.time.LocalDateTime

data class ChatMessage(
    val id: Long,
    val roomId: Long,
    val senderType: ChatMessageSenderType,
    val content: String,
    val messageOrder: Int,
    val isCrisisDetected: Boolean,
    val createdAt: LocalDateTime? = null,
)
