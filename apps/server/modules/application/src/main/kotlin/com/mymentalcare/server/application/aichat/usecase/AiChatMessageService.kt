package com.mymentalcare.server.application.aichat.usecase

import com.mymentalcare.server.application.aichat.AiChatInvalidRequestException
import com.mymentalcare.server.application.aichat.AiChatResponseAssembler
import com.mymentalcare.server.application.aichat.policy.CrisisKeywordDetectionResult
import com.mymentalcare.server.application.aichat.policy.CrisisKeywordDetector
import com.mymentalcare.server.application.aichat.policy.SAFETY_GUIDE_MESSAGE
import com.mymentalcare.server.application.aichat.port.AiChatMessageInputPort
import com.mymentalcare.server.application.aichat.port.AiReplyProvider
import com.mymentalcare.server.application.aichat.reader.AiChatSegmentContextReader
import com.mymentalcare.server.application.aichat.reader.AiReplyContextReader
import com.mymentalcare.server.application.aichat.reader.TodayAiChatRoomReader
import com.mymentalcare.server.application.aichat.recorder.AiChatMessageAppender
import com.mymentalcare.server.application.aichat.recorder.AiChatOpeningMessageRecorder
import com.mymentalcare.server.application.aichat.recorder.AiChatSummaryRefreshProcessor
import com.mymentalcare.server.application.aichat.recorder.CrisisDetectionRecorder
import com.mymentalcare.server.application.aichat.request.AiReplyRequest
import com.mymentalcare.server.application.aichat.request.SendAiChatMessageRequest
import com.mymentalcare.server.application.aichat.request.StartAiChatSegmentRequest
import com.mymentalcare.server.application.aichat.response.AiReplyResponse
import com.mymentalcare.server.application.aichat.response.SendAiChatMessageResponse
import com.mymentalcare.server.application.aichat.response.StartAiChatSegmentResponse
import com.mymentalcare.server.domain.aichat.AiChatSegmentStartType
import com.mymentalcare.server.domain.aichat.CrisisRiskLevel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class AiChatMessageService(
    private val todayAiChatRoomReader: TodayAiChatRoomReader,
    private val aiChatSegmentStarter: AiChatSegmentStarter,
    private val aiChatOpeningMessageFactory: com.mymentalcare.server.application.aichat.policy.AiChatOpeningMessageFactory,
    private val aiChatOpeningMessageRecorder: AiChatOpeningMessageRecorder,
    private val aiChatMessageAppender: AiChatMessageAppender,
    private val aiChatSummaryRefreshProcessor: AiChatSummaryRefreshProcessor,
    private val aiReplyContextReader: AiReplyContextReader,
    private val aiChatSegmentContextReader: AiChatSegmentContextReader,
    private val crisisDetectionRecorder: CrisisDetectionRecorder,
    private val aiChatResponseAssembler: AiChatResponseAssembler,
    private val crisisKeywordDetector: CrisisKeywordDetector,
    private val aiReplyProvider: AiReplyProvider,
) : AiChatMessageInputPort {
    // 체크인 없이 오늘 대화방 안에 새 주제 구간을 시작한다.
    @Transactional
    override fun startSegment(memberId: Long, request: StartAiChatSegmentRequest): StartAiChatSegmentResponse {
        if (request.startType != AiChatSegmentStartType.DIRECT.name) {
            throw AiChatInvalidRequestException("바로 상담 시작만 지원하는 요청입니다.")
        }

        val room = todayAiChatRoomReader.readOrCreateTodayRoom(memberId)
        val segment = aiChatSegmentStarter.startDirectSegment(room, request.clientRequestId)
        val assistantMessage = aiChatOpeningMessageRecorder.recordOpeningMessageIfNeeded(
            room = room,
            segment = segment,
            content = aiChatOpeningMessageFactory.buildDirectOpeningMessage(),
            crisisDetected = false,
        )
        aiReplyContextReader.appendMessagesToRecentCache(room, listOf(assistantMessage))

        return aiChatResponseAssembler.toStartSegmentResponse(
            room = room,
            segment = segment,
            checkIn = null,
            assistantMessage = assistantMessage,
            detection = CrisisKeywordDetectionResult(detected = false, keywords = emptyList(), riskLevel = CrisisRiskLevel.LOW),
        )
    }

    // 사용자 메시지를 저장하고 위기 감지 또는 AI 응답 생성 결과를 함께 저장한다.
    @Transactional
    override fun sendMessage(memberId: Long, request: SendAiChatMessageRequest): SendAiChatMessageResponse {
        val room = todayAiChatRoomReader.readOrCreateTodayRoom(memberId)
        val segment = aiChatSegmentStarter.readActiveSegmentOrStartDirectSegment(room, request.segmentId)
        val detection = crisisKeywordDetector.detectCrisisKeywords(request.content)

        val userMessage = aiChatMessageAppender.appendUserMessage(
            room = room,
            segmentId = segment.id,
            content = request.content,
            crisisDetected = detection.detected,
        )

        if (detection.detected) {
            crisisDetectionRecorder.recordSafetyModalShown(
                memberId = memberId,
                room = room,
                message = userMessage,
                detection = detection,
            )
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
                    segmentContext = aiChatSegmentContextReader.readSegmentContext(segment),
                    checkInContext = aiChatSegmentContextReader.readCheckInContext(segment),
                    summaryContext = aiReplyContextReader.readTodaySummaryContext(room),
                    recentMessages = aiReplyContextReader.readRecentMessagesForReply(room, userMessage),
                )
            )
        }

        val assistantMessage = aiChatMessageAppender.appendAssistantMessage(
            room = room,
            segmentId = segment.id,
            content = aiReply.content,
            crisisDetected = detection.detected,
        )
        val messagesToCache = if (detection.detected) listOf(userMessage, assistantMessage) else listOf(assistantMessage)
        aiReplyContextReader.appendMessagesToRecentCache(room, messagesToCache)

        return aiChatResponseAssembler.toSendMessageResponse(
            room,
            userMessage,
            assistantMessage,
            detection,
            aiReply.failed,
        )
    }
}
