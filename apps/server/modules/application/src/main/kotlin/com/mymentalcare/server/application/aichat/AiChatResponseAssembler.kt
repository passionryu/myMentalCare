package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.application.port.ChatMessageRepository
import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.ChatMessage
import org.springframework.stereotype.Component

@Component
internal class AiChatResponseAssembler(
    private val chatMessageRepository: ChatMessageRepository,
) {
    // 현재 대화방과 저장된 메시지 목록을 화면 응답으로 변환한다.
    fun toTodayRoomResponse(room: AiChatRoom): TodayAiChatRoomResponse {
        return room.toResponse(chatMessageRepository.findByRoomId(room.id))
    }

    // 메시지 전송 결과를 사용자 메시지, 챗봇 응답, 위기 감지 결과로 묶어 반환한다.
    fun toSendMessageResponse(
        room: AiChatRoom,
        userMessage: ChatMessage,
        assistantMessage: ChatMessage,
        detection: CrisisKeywordDetectionResult,
        aiReplyFailed: Boolean,
    ): SendAiChatMessageResponse {
        return SendAiChatMessageResponse(
            room = toTodayRoomResponse(room),
            userMessage = userMessage.toResponse(),
            assistantMessage = assistantMessage.toResponse(),
            crisisDetected = detection.detected,
            crisisGuideMessage = if (detection.detected) SAFETY_GUIDE_MESSAGE else null,
            aiReplyFailed = aiReplyFailed,
            aiReplyErrorMessage = if (aiReplyFailed) OPEN_AI_REPLY_ERROR_MESSAGE else null,
        )
    }

    private fun AiChatRoom.toResponse(messages: List<ChatMessage>): TodayAiChatRoomResponse {
        return TodayAiChatRoomResponse(
            roomId = id,
            chatbotCode = chatbotCode,
            chatbotName = "마음이",
            conversationDate = conversationDate,
            status = status.name,
            messages = messages.map { it.toResponse() },
        )
    }

    private fun ChatMessage.toResponse(): AiChatMessageResponse {
        return AiChatMessageResponse(
            messageId = id,
            senderType = senderType.name,
            content = content,
            messageOrder = messageOrder,
            isCrisisDetected = isCrisisDetected,
            createdAt = createdAt,
        )
    }
}
