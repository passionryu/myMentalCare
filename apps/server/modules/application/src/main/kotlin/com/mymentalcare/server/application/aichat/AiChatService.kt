package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.application.port.AiChatCheckInRepository
import com.mymentalcare.server.application.port.AiChatReportRepository
import com.mymentalcare.server.application.port.AiChatRoomRepository
import com.mymentalcare.server.application.port.AiChatSegmentRepository
import com.mymentalcare.server.application.port.ChatMessageRepository
import com.mymentalcare.server.domain.aichat.AiChatReport
import com.mymentalcare.server.domain.aichat.AiChatReportSong
import com.mymentalcare.server.domain.aichat.AiChatReportType
import com.mymentalcare.server.domain.aichat.AiChatCheckInTemplateType
import com.mymentalcare.server.domain.aichat.AiChatSegmentStartType
import com.mymentalcare.server.domain.aichat.ChatMessage
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
    private val aiChatRoomRepository: AiChatRoomRepository,
    private val aiChatSegmentRepository: AiChatSegmentRepository,
    private val aiChatCheckInRepository: AiChatCheckInRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val aiChatReportRepository: AiChatReportRepository,
    private val aiChatReportReadinessDecider: AiChatReportReadinessDecider,
    private val aiChatReportGenerator: AiChatReportGenerator,
    private val crisisKeywordDetector: CrisisKeywordDetector,
    private val aiReplyProvider: AiReplyProvider,
) : AiChatInputPort {
    // 오늘의 AI 마음 대화방을 조회하거나 없으면 생성한다.
    @Transactional
    override fun readTodayRoom(memberId: Long): TodayAiChatRoomResponse {
        val room = todayAiChatRoomReader.readOrCreateTodayRoom(memberId)

        return aiChatResponseAssembler.toTodayRoomResponse(room)
    }

    @Transactional(readOnly = true)
    override fun readHistoryRooms(memberId: Long): List<AiChatHistoryRoomResponse> {
        return aiChatRoomRepository.findByMemberId(memberId).map { room ->
            val latestMessage = chatMessageRepository.findLatestByRoomId(room.id)
            AiChatHistoryRoomResponse(
                roomId = room.id,
                conversationDate = room.conversationDate,
                status = room.status.name,
                messageCount = chatMessageRepository.countByRoomId(room.id),
                latestMessage = latestMessage?.content,
                latestMessageAt = latestMessage?.createdAt,
            )
        }
    }

    @Transactional(readOnly = true)
    override fun readHistoryRoom(memberId: Long, roomId: Long): AiChatHistoryRoomDetailResponse? {
        val room = aiChatRoomRepository.findByIdAndMemberId(roomId = roomId, memberId = memberId) ?: return null
        return AiChatHistoryRoomDetailResponse(
            roomId = room.id,
            chatbotCode = room.chatbotCode,
            chatbotName = "마음이",
            conversationDate = room.conversationDate,
            status = room.status.name,
            messages = chatMessageRepository.findByRoomId(room.id).map { it.toResponse() },
        )
    }

    @Transactional(readOnly = true)
    override fun readReports(memberId: Long): List<AiChatReportResponse> {
        return aiChatReportRepository.findByMemberId(memberId).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    override fun readReport(memberId: Long, reportId: Long): AiChatReportResponse? {
        return aiChatReportRepository.findByIdAndMemberId(reportId = reportId, memberId = memberId)?.toResponse()
    }

    @Transactional(readOnly = true)
    override fun readCheckIns(memberId: Long): List<AiChatCheckInHistoryResponse> {
        val rooms = aiChatRoomRepository.findByMemberId(memberId)
        val segments = rooms.flatMap { aiChatSegmentRepository.findByRoomId(it.id) }
        val segmentById = segments.associateBy { it.id }
        val roomIdBySegmentId = segments.associate { it.id to it.roomId }

        return aiChatCheckInRepository.findBySegmentIds(segments.map { it.id })
            .sortedByDescending { it.createdAt ?: java.time.LocalDateTime.MIN }
            .map { checkIn ->
                AiChatCheckInHistoryResponse(
                    checkInId = checkIn.id,
                    roomId = roomIdBySegmentId[checkIn.segmentId] ?: 0,
                    segmentId = checkIn.segmentId,
                    templateType = checkIn.templateType.name,
                    summaryText = checkIn.summaryText,
                    answers = checkIn.answers.map { answer ->
                        AiChatCheckInAnswerResponse(
                            stepKey = answer.stepKey,
                            optionKey = answer.optionKey,
                            label = answer.label,
                            value = answer.value,
                            freeText = answer.freeText,
                        )
                    },
                    createdAt = checkIn.createdAt ?: segmentById[checkIn.segmentId]?.startedAt,
                )
            }
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

    // 오늘 대화가 리포트를 만들 만큼 충분한지 판단한다.
    @Transactional(readOnly = true)
    override fun readTodayReportReadiness(memberId: Long): AiChatReportReadinessResponse {
        val room = todayAiChatRoomReader.readTodayRoom(memberId)
        val messages = room?.let { aiChatResponseAssembler.readMessages(it) }.orEmpty()
        val readiness = aiChatReportReadinessDecider.decide(messages)

        return readiness.toResponse()
    }

    // 오늘 대화를 FULL 또는 SHORT 리포트로 정리하고 생성 즉시 저장한다.
    @Transactional
    override fun createTodayReport(memberId: Long, request: CreateAiChatReportRequest): AiChatReportResponse {
        val room = todayAiChatRoomReader.readOrCreateTodayRoom(memberId)
        val existingRequestReport = request.clientRequestId
            ?.let { aiChatReportRepository.findByRoomIdAndClientRequestId(room.id, it) }
        if (existingRequestReport != null) {
            return existingRequestReport.toResponse()
        }

        val existingReport = aiChatReportRepository.findLatestByRoomId(room.id)
        if (existingReport != null) {
            return existingReport.toResponse()
        }

        val messages = aiChatResponseAssembler.readMessages(room)
        val readiness = aiChatReportReadinessDecider.decide(messages)
        if (!readiness.ready && !request.forceCreate) {
            throw AiChatInvalidRequestException(readiness.guideMessage ?: SHORT_REPORT_GUIDE_MESSAGE)
        }

        val reportType = if (readiness.ready) AiChatReportType.FULL else AiChatReportType.SHORT
        val draft = aiChatReportGenerator.generateReport(reportType, messages)
        val savedReport = aiChatReportRepository.save(
            AiChatReport(
                id = 0,
                roomId = room.id,
                memberId = memberId,
                conversationDate = room.conversationDate,
                reportType = reportType,
                summary = draft.summary,
                primaryEmotion = draft.primaryEmotion,
                emotionIntensity = draft.emotionIntensity,
                mainCause = draft.mainCause,
                emotionalFlow = draft.emotionalFlow,
                todaySentence = draft.todaySentence,
                clientRequestId = request.clientRequestId,
                songs = draft.songs,
            )
        )

        return savedReport.toResponse()
    }

    // 오늘 저장된 최신 마음 리포트를 조회한다.
    @Transactional(readOnly = true)
    override fun readLatestTodayReport(memberId: Long): AiChatReportResponse? {
        val room = todayAiChatRoomReader.readTodayRoom(memberId) ?: return null

        return aiChatReportRepository.findLatestByRoomId(room.id)?.toResponse()
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

    private fun AiChatReportReadinessResult.toResponse(): AiChatReportReadinessResponse {
        return AiChatReportReadinessResponse(
            ready = ready,
            reason = reason,
            userMessageCount = userMessageCount,
            userTextLength = userTextLength,
            requiredUserMessageCount = requiredUserMessageCount,
            requiredUserTextLength = requiredUserTextLength,
            unmetRequirements = unmetRequirements,
            guideMessage = guideMessage,
        )
    }

    private fun AiChatReport.toResponse(): AiChatReportResponse {
        return AiChatReportResponse(
            reportId = id,
            roomId = roomId,
            reportType = reportType.name,
            conversationDate = conversationDate,
            summary = summary,
            primaryEmotion = primaryEmotion,
            emotionIntensity = emotionIntensity,
            mainCause = mainCause,
            emotionalFlow = emotionalFlow,
            todaySentence = todaySentence,
            songs = songs.map { it.toResponse() },
            saved = true,
            createdAt = createdAt,
        )
    }

    private fun AiChatReportSong.toResponse(): AiChatReportSongResponse {
        return AiChatReportSongResponse(
            title = title,
            artist = artist,
            reason = reason,
            youtubeUrl = youtubeUrl,
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
