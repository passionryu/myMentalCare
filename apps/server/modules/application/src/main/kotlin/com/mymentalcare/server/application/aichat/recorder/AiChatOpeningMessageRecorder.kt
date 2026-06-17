package com.mymentalcare.server.application.aichat.recorder

import com.mymentalcare.server.application.aichat.policy.*
import com.mymentalcare.server.application.aichat.port.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.AiChatSegment
import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import org.springframework.stereotype.Component

@Component
internal class AiChatOpeningMessageRecorder(
    private val chatMessageRepository: ChatMessageRepository,
    private val aiChatMessageAppender: AiChatMessageAppender,
) {
    // segment 시작 요청이 재시도되어도 마음이의 첫 메시지가 중복 저장되지 않게 한다.
    fun recordOpeningMessageIfNeeded(
        room: AiChatRoom,
        segment: AiChatSegment,
        content: String,
        crisisDetected: Boolean,
    ): ChatMessage {
        val existingOpeningMessage = chatMessageRepository.findBySegmentId(segment.id)
            .firstOrNull { it.senderType == ChatMessageSenderType.ASSISTANT }
        if (existingOpeningMessage != null) {
            return existingOpeningMessage
        }

        return aiChatMessageAppender.appendAssistantMessage(
            room = room,
            segmentId = segment.id,
            content = content,
            crisisDetected = crisisDetected,
        )
    }
}
