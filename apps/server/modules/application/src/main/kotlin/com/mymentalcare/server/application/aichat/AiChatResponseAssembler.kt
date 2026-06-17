package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.application.aichat.policy.*
import com.mymentalcare.server.application.aichat.port.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.AiChatCheckIn
import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.AiChatSegment
import com.mymentalcare.server.domain.aichat.ChatMessage
import org.springframework.stereotype.Component

@Component
internal class AiChatResponseAssembler(
    private val chatMessageRepository: ChatMessageRepository,
    private val aiChatSegmentRepository: AiChatSegmentRepository,
    private val aiChatCheckInRepository: AiChatCheckInRepository,
) {
    // 현재 대화방과 저장된 메시지 목록을 화면 응답으로 변환한다.
    fun toTodayRoomResponse(room: AiChatRoom): TodayAiChatRoomResponse {
        return room.toResponse(
            segments = aiChatSegmentRepository.findByRoomId(room.id),
            messages = readMessages(room),
        )
    }

    fun readMessages(room: AiChatRoom): List<ChatMessage> {
        return chatMessageRepository.findByRoomId(room.id)
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

    fun toStartSegmentResponse(
        room: AiChatRoom,
        segment: AiChatSegment,
        checkIn: AiChatCheckIn?,
        assistantMessage: ChatMessage,
        detection: CrisisKeywordDetectionResult,
    ): StartAiChatSegmentResponse {
        return StartAiChatSegmentResponse(
            room = toTodayRoomResponse(room),
            segment = segment.toResponse(checkIn),
            checkIn = checkIn?.toResponse(),
            assistantMessage = assistantMessage.toResponse(),
            crisisDetected = detection.detected,
            crisisGuideMessage = if (detection.detected) SAFETY_GUIDE_MESSAGE else null,
        )
    }

    private fun AiChatRoom.toResponse(segments: List<AiChatSegment>, messages: List<ChatMessage>): TodayAiChatRoomResponse {
        val checkInsBySegmentId = aiChatCheckInRepository.findBySegmentIds(segments.map { it.id })
            .associateBy { it.segmentId }
        return TodayAiChatRoomResponse(
            roomId = id,
            chatbotCode = chatbotCode,
            chatbotName = "마음이",
            conversationDate = conversationDate,
            status = status.name,
            hasConversation = segments.isNotEmpty() || messages.isNotEmpty(),
            activeSegmentId = segments.maxByOrNull { it.segmentOrder }?.id,
            segments = segments.map { it.toResponse(checkInsBySegmentId[it.id]) },
            messages = messages.map { it.toResponse() },
        )
    }

    private fun AiChatSegment.toResponse(checkIn: AiChatCheckIn?): AiChatSegmentResponse {
        return AiChatSegmentResponse(
            segmentId = id,
            segmentOrder = segmentOrder,
            startType = startType.name,
            title = title,
            startedAt = startedAt,
            checkIn = checkIn?.toResponse(),
        )
    }

    private fun AiChatCheckIn.toResponse(): AiChatCheckInResponse {
        return AiChatCheckInResponse(
            checkInId = id,
            templateType = templateType.name,
            summaryText = summaryText,
        )
    }

    private fun ChatMessage.toResponse(): AiChatMessageResponse {
        return AiChatMessageResponse(
            messageId = id,
            segmentId = segmentId,
            senderType = senderType.name,
            content = content,
            messageOrder = messageOrder,
            isCrisisDetected = isCrisisDetected,
            createdAt = createdAt,
        )
    }
}
