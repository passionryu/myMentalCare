package com.mymentalcare.server.bootstrap.aichat

import com.mymentalcare.server.application.aichat.AiChatMessageResponse as ApplicationAiChatMessageResponse
import com.mymentalcare.server.application.aichat.SendAiChatMessageResponse as ApplicationSendAiChatMessageResponse
import com.mymentalcare.server.application.aichat.TodayAiChatRoomResponse as ApplicationTodayAiChatRoomResponse

fun ApplicationTodayAiChatRoomResponse.toBootstrapResponse(): TodayAiChatRoomResponse {
    return TodayAiChatRoomResponse(
        roomId = roomId,
        chatbotCode = chatbotCode,
        chatbotName = chatbotName,
        conversationDate = conversationDate,
        status = status,
        messages = messages.map { it.toBootstrapResponse() },
    )
}

fun ApplicationSendAiChatMessageResponse.toBootstrapResponse(): SendAiChatMessageResponse {
    return SendAiChatMessageResponse(
        room = room.toBootstrapResponse(),
        userMessage = userMessage.toBootstrapResponse(),
        assistantMessage = assistantMessage.toBootstrapResponse(),
        crisisDetected = crisisDetected,
        crisisGuideMessage = crisisGuideMessage,
        aiReplyFailed = aiReplyFailed,
        aiReplyErrorMessage = aiReplyErrorMessage,
    )
}

private fun ApplicationAiChatMessageResponse.toBootstrapResponse(): AiChatMessageResponse {
    return AiChatMessageResponse(
        messageId = messageId,
        senderType = senderType,
        content = content,
        messageOrder = messageOrder,
        isCrisisDetected = isCrisisDetected,
        createdAt = createdAt,
    )
}
