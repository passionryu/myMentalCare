package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.domain.aichat.ChatMessageSenderType

data class AiReplyRequest(
    val memberId: Long,
    val roomId: Long,
    val messageId: Long,
    val segmentContext: AiChatSegmentContext? = null,
    val checkInContext: AiChatCheckInContext? = null,
    val summaryContext: AiChatSummaryContext? = null,
    val recentMessages: List<AiReplyMessage> = emptyList(),
)

data class AiReplyMessage(
    val senderType: ChatMessageSenderType,
    val content: String,
)

data class AiChatSegmentContext(
    val title: String,
    val startType: String,
)

data class AiChatCheckInContext(
    val templateType: String,
    val summaryText: String,
)
