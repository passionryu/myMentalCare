package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.domain.aichat.AiChatCheckInTemplateType
import com.mymentalcare.server.domain.aichat.AiChatSegmentStartType
import com.mymentalcare.server.domain.aichat.CrisisRiskLevel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class AiChatService(
    private val todayAiChatRoomReader: TodayAiChatRoomReader,
    private val aiChatSegmentStarter: AiChatSegmentStarter,
    private val aiChatCheckInSummaryFactory: AiChatCheckInSummaryFactory,
    private val aiChatCheckInRecorder: AiChatCheckInRecorder,
    private val aiChatOpeningMessageFactory: AiChatOpeningMessageFactory,
    private val aiChatOpeningMessageRecorder: AiChatOpeningMessageRecorder,
    private val aiChatMessageAppender: AiChatMessageAppender,
    private val aiChatSummaryRefreshProcessor: AiChatSummaryRefreshProcessor,
    private val aiReplyContextReader: AiReplyContextReader,
    private val aiChatSegmentContextReader: AiChatSegmentContextReader,
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

    // 체크인 답변을 저장하고 해당 맥락으로 오늘 대화방 안에 새 구간을 시작한다.
    @Transactional
    override fun startCheckInSegment(memberId: Long, request: StartAiChatCheckInRequest): StartAiChatSegmentResponse {
        validateCheckInAnswers(request)

        val room = todayAiChatRoomReader.readOrCreateTodayRoom(memberId)
        val summaryText = aiChatCheckInSummaryFactory.buildSummaryText(request.templateType, request.answers)
        val segment = aiChatSegmentStarter.startCheckInSegment(
            room = room,
            startType = request.templateType.toSegmentStartType(),
            summaryText = summaryText,
            clientRequestId = request.clientRequestId,
        )
        val detection = detectCrisisKeywordsFromCheckIn(request)
        val checkIn = aiChatCheckInRecorder.recordCheckIn(
            segmentId = segment.id,
            templateType = request.templateType,
            answers = request.answers,
            summaryText = summaryText,
            detection = detection,
        )

        if (detection.detected) {
            crisisDetectionRecorder.recordCheckInSafetyModalShown(
                memberId = memberId,
                room = room,
                checkIn = checkIn,
                detection = detection,
            )
        }

        val openingMessage = if (detection.detected) {
            SAFETY_GUIDE_MESSAGE
        } else {
            aiChatOpeningMessageFactory.buildCheckInOpeningMessage(request.templateType, summaryText)
        }
        val assistantMessage = aiChatOpeningMessageRecorder.recordOpeningMessageIfNeeded(
            room = room,
            segment = segment,
            content = openingMessage,
            crisisDetected = detection.detected,
        )
        aiReplyContextReader.appendMessagesToRecentCache(room, listOf(assistantMessage))

        return aiChatResponseAssembler.toStartSegmentResponse(
            room = room,
            segment = segment,
            checkIn = checkIn,
            assistantMessage = assistantMessage,
            detection = detection,
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

    private fun validateCheckInAnswers(request: StartAiChatCheckInRequest) {
        if (request.answers.isEmpty()) {
            throw AiChatInvalidRequestException("체크인 답변을 입력해주세요.")
        }
        if (request.answers.any { it.optionKey == "OTHER" && it.freeText.isNullOrBlank() }) {
            throw AiChatInvalidRequestException("기타를 선택한 경우 직접 입력이 필요합니다.")
        }
    }

    private fun detectCrisisKeywordsFromCheckIn(request: StartAiChatCheckInRequest): CrisisKeywordDetectionResult {
        val checkInText = request.answers
            .mapNotNull { it.freeText?.trim()?.takeIf { text -> text.isNotBlank() } }
            .joinToString(" ")

        return crisisKeywordDetector.detectCrisisKeywords(checkInText)
    }

    private fun AiChatCheckInTemplateType.toSegmentStartType(): AiChatSegmentStartType {
        return when (this) {
            AiChatCheckInTemplateType.BASIC_EMOTION -> AiChatSegmentStartType.BASIC_EMOTION
            AiChatCheckInTemplateType.CONVERSATION_START -> AiChatSegmentStartType.CONVERSATION_START
            AiChatCheckInTemplateType.CONDITION -> AiChatSegmentStartType.CONDITION
            AiChatCheckInTemplateType.DAY_REVIEW -> AiChatSegmentStartType.DAY_REVIEW
        }
    }
}
