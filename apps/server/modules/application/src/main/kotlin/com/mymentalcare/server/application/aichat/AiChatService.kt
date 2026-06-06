package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.application.port.AiChatRoomRepository
import com.mymentalcare.server.application.port.ChatMessageRepository
import com.mymentalcare.server.application.port.CrisisDetectionEventRepository
import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.AiChatRoomStatus
import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import com.mymentalcare.server.domain.aichat.CrisisDetectionEvent
import com.mymentalcare.server.domain.aichat.CrisisHandledAction
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

private const val DEFAULT_EMPATHY_CHATBOT_CODE = "DEFAULT_EMPATHY"
private val KOREA_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")

@Service
internal class AiChatService(
    private val aiChatRoomRepository: AiChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val crisisDetectionEventRepository: CrisisDetectionEventRepository,
    private val crisisKeywordDetector: CrisisKeywordDetector,
    private val defaultEmpathyReplyProvider: DefaultEmpathyReplyProvider,
) : AiChatInputPort {
    // 오늘의 AI 마음 대화방을 조회하거나 없으면 생성한다.
    @Transactional
    override fun readTodayRoom(memberId: Long): TodayAiChatRoomResponse {
        val room = readOrCreateTodayRoom(memberId)
        val messages = chatMessageRepository.findByRoomId(room.id)

        return room.toResponse(messages)
    }

    // 사용자 메시지를 저장하고 기본 공감형 챗봇 응답을 함께 저장한다.
    @Transactional
    override fun sendMessage(memberId: Long, request: SendAiChatMessageRequest): SendAiChatMessageResponse {
        val room = readOrCreateTodayRoom(memberId)
        val nextOrder = chatMessageRepository.countByRoomId(room.id) + 1
        val detection = crisisKeywordDetector.detectCrisisKeywords(request.content)

        val userMessage = chatMessageRepository.save(
            ChatMessage(
                id = 0,
                roomId = room.id,
                senderType = ChatMessageSenderType.USER,
                content = request.content.trim(),
                messageOrder = nextOrder,
                isCrisisDetected = detection.detected,
            )
        )

        if (detection.detected) {
            crisisDetectionEventRepository.save(
                CrisisDetectionEvent(
                    id = 0,
                    memberId = memberId,
                    roomId = room.id,
                    messageId = userMessage.id,
                    detectedKeywords = detection.keywords,
                    riskLevel = detection.riskLevel,
                    handledAction = CrisisHandledAction.SAFETY_MODAL_SHOWN,
                )
            )
        }

        val assistantMessage = chatMessageRepository.save(
            ChatMessage(
                id = 0,
                roomId = room.id,
                senderType = ChatMessageSenderType.ASSISTANT,
                content = defaultEmpathyReplyProvider.replyFor(nextOrder, detection.detected),
                messageOrder = nextOrder + 1,
                isCrisisDetected = detection.detected,
            )
        )

        return SendAiChatMessageResponse(
            room = room.toResponse(chatMessageRepository.findByRoomId(room.id)),
            userMessage = userMessage.toResponse(),
            assistantMessage = assistantMessage.toResponse(),
            crisisDetected = detection.detected,
            crisisGuideMessage = if (detection.detected) SAFETY_GUIDE_MESSAGE else null,
        )
    }

    private fun readOrCreateTodayRoom(memberId: Long): AiChatRoom {
        val today = LocalDate.now(KOREA_ZONE_ID)
        return aiChatRoomRepository.findTodayRoom(memberId, DEFAULT_EMPATHY_CHATBOT_CODE, today)
            ?: aiChatRoomRepository.save(
                AiChatRoom(
                    id = 0,
                    memberId = memberId,
                    chatbotCode = DEFAULT_EMPATHY_CHATBOT_CODE,
                    conversationDate = today,
                    status = AiChatRoomStatus.ACTIVE,
                )
            )
    }
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
