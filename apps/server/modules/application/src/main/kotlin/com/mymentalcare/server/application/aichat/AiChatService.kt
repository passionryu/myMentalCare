package com.mymentalcare.server.application.aichat

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class AiChatService(
    private val todayAiChatRoomReader: TodayAiChatRoomReader,
    private val aiChatMessageAppender: AiChatMessageAppender,
    private val aiChatSummaryRefreshProcessor: AiChatSummaryRefreshProcessor,
    private val aiReplyContextReader: AiReplyContextReader,
    private val crisisDetectionRecorder: CrisisDetectionRecorder,
    private val aiChatResponseAssembler: AiChatResponseAssembler,
    private val crisisKeywordDetector: CrisisKeywordDetector,
    private val aiReplyProvider: AiReplyProvider,
) : AiChatInputPort {
    // 오늘의 AI 마음 대화방을 조회하거나 없으면 생성한다.
    @Transactional
    override fun readTodayRoom(memberId: Long): TodayAiChatRoomResponse {
        val room = todayAiChatRoomReader.readOrCreateTodayRoom(memberId)

        return aiChatResponseAssembler.toTodayRoomResponse(room)
    }

    // 사용자 메시지를 저장하고 위기 감지 또는 AI 응답 생성 결과를 함께 저장한다.
    @Transactional
    override fun sendMessage(memberId: Long, request: SendAiChatMessageRequest): SendAiChatMessageResponse {
        val room = todayAiChatRoomReader.readOrCreateTodayRoom(memberId)

        val detection = crisisKeywordDetector.detectCrisisKeywords(request.content)

        val userMessage = aiChatMessageAppender.appendUserMessage(
            room,
            request.content,
            detection.detected)

        if (detection.detected) {
            crisisDetectionRecorder.recordSafetyModalShown(
                memberId,
                room,
                userMessage,
                detection)
        }

        val aiReply = if (detection.detected) {
            AiReplyResponse(SAFETY_GUIDE_MESSAGE)
        } else {
            aiChatSummaryRefreshProcessor.refreshTodaySummaryIfNeeded(room)

            aiReplyProvider.generateReply(
                AiReplyRequest(
                    memberId = memberId,
                    roomId = room.id,
                    messageId = userMessage.id,
                    summaryContext = aiReplyContextReader.readTodaySummaryContext(room),
                    recentMessages = aiReplyContextReader.readRecentMessagesForReply(room, userMessage),
                )
            )
        }

        val assistantMessage = aiChatMessageAppender.appendAssistantMessage(
            room,
            aiReply.content,
            detection.detected)
        val messagesToCache = if (detection.detected) listOf(userMessage, assistantMessage) else listOf(assistantMessage)
        aiReplyContextReader.appendMessagesToRecentCache(room, messagesToCache)

        return aiChatResponseAssembler.toSendMessageResponse(
            room,
            userMessage,
            assistantMessage,
            detection,
            aiReply.failed)
    }
}
