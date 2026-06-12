package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.application.port.ChatMessageRepository
import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import org.springframework.stereotype.Component

@Component
internal class AiChatMessageAppender(
    private val chatMessageRepository: ChatMessageRepository,
) {
    // 사용자가 보낸 메시지를 현재 대화방의 다음 순서로 저장한다.
    fun appendUserMessage(room: AiChatRoom, segmentId: Long?, content: String, crisisDetected: Boolean): ChatMessage {
        return appendMessage(room, segmentId, ChatMessageSenderType.USER, content.trim(), crisisDetected)
    }

    // 챗봇 응답 메시지를 현재 대화방의 다음 순서로 저장한다.
    fun appendAssistantMessage(room: AiChatRoom, segmentId: Long?, content: String, crisisDetected: Boolean): ChatMessage {
        return appendMessage(room, segmentId, ChatMessageSenderType.ASSISTANT, content, crisisDetected)
    }

    private fun appendMessage(
        room: AiChatRoom,
        segmentId: Long?,
        senderType: ChatMessageSenderType,
        content: String,
        crisisDetected: Boolean,
    ): ChatMessage {
        return chatMessageRepository.save(
            ChatMessage(
                id = 0,
                roomId = room.id,
                segmentId = segmentId,
                senderType = senderType,
                content = content,
                messageOrder = chatMessageRepository.countByRoomId(room.id) + 1,
                isCrisisDetected = crisisDetected,
            )
        )
    }
}
