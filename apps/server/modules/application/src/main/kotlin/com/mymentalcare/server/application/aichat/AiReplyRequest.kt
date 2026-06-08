package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.domain.aichat.ChatMessageSenderType

data class AiReplyRequest(
    val memberId: Long,
    val roomId: Long,
    val messageId: Long,
    val recentMessages: List<AiReplyMessage>,
)

data class AiReplyMessage(
    val senderType: ChatMessageSenderType,
    val content: String,
)
