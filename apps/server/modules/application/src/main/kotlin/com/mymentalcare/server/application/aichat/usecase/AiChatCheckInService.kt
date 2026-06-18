package com.mymentalcare.server.application.aichat.usecase

import com.mymentalcare.server.application.aichat.AiChatInvalidRequestException
import com.mymentalcare.server.application.aichat.AiChatResponseAssembler
import com.mymentalcare.server.application.aichat.policy.AiChatCheckInSummaryFactory
import com.mymentalcare.server.application.aichat.policy.AiChatOpeningMessageFactory
import com.mymentalcare.server.application.aichat.policy.CrisisKeywordDetectionResult
import com.mymentalcare.server.application.aichat.policy.CrisisKeywordDetector
import com.mymentalcare.server.application.aichat.policy.SAFETY_GUIDE_MESSAGE
import com.mymentalcare.server.application.aichat.port.AiChatCheckInInputPort
import com.mymentalcare.server.application.aichat.port.AiChatCheckInRepository
import com.mymentalcare.server.application.aichat.port.AiChatRoomRepository
import com.mymentalcare.server.application.aichat.port.AiChatSegmentRepository
import com.mymentalcare.server.application.aichat.reader.AiReplyContextReader
import com.mymentalcare.server.application.aichat.reader.TodayAiChatRoomReader
import com.mymentalcare.server.application.aichat.recorder.AiChatCheckInRecorder
import com.mymentalcare.server.application.aichat.recorder.AiChatOpeningMessageRecorder
import com.mymentalcare.server.application.aichat.recorder.CrisisDetectionRecorder
import com.mymentalcare.server.application.aichat.request.StartAiChatCheckInRequest
import com.mymentalcare.server.application.aichat.response.AiChatCheckInAnswerResponse
import com.mymentalcare.server.application.aichat.response.AiChatCheckInHistoryResponse
import com.mymentalcare.server.application.aichat.response.StartAiChatSegmentResponse
import com.mymentalcare.server.domain.aichat.AiChatCheckInTemplateType
import com.mymentalcare.server.domain.aichat.AiChatSegmentStartType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class AiChatCheckInService(
    private val todayAiChatRoomReader: TodayAiChatRoomReader,
    private val aiChatSegmentStarter: AiChatSegmentStarter,
    private val aiChatCheckInSummaryFactory: AiChatCheckInSummaryFactory,
    private val aiChatCheckInRecorder: AiChatCheckInRecorder,
    private val aiChatOpeningMessageFactory: AiChatOpeningMessageFactory,
    private val aiChatOpeningMessageRecorder: AiChatOpeningMessageRecorder,
    private val aiReplyContextReader: AiReplyContextReader,
    private val crisisDetectionRecorder: CrisisDetectionRecorder,
    private val aiChatResponseAssembler: AiChatResponseAssembler,
    private val aiChatRoomRepository: AiChatRoomRepository,
    private val aiChatSegmentRepository: AiChatSegmentRepository,
    private val aiChatCheckInRepository: AiChatCheckInRepository,
    private val crisisKeywordDetector: CrisisKeywordDetector,
) : AiChatCheckInInputPort {
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
