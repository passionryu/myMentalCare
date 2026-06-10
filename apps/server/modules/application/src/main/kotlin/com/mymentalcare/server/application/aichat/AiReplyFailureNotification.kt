package com.mymentalcare.server.application.aichat

import java.time.LocalDateTime

data class AiReplyFailureNotification(
    val occurredAt: LocalDateTime,
    val environment: String,
    val model: String,
    val memberId: Long,
    val roomId: Long,
    val messageId: Long,
    val failureType: String,
    val fallbackUsed: Boolean,
)
