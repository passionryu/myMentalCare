package com.mymentalcare.server.application.aichat.usecase

import com.mymentalcare.server.application.aichat.AiChatInvalidRequestException
import com.mymentalcare.server.application.aichat.AiChatResponseAssembler
import com.mymentalcare.server.application.aichat.policy.AiChatReportReadinessDecider
import com.mymentalcare.server.application.aichat.policy.SHORT_REPORT_GUIDE_MESSAGE
import com.mymentalcare.server.application.aichat.port.AiChatReportGenerator
import com.mymentalcare.server.application.aichat.port.AiChatReportInputPort
import com.mymentalcare.server.application.aichat.port.AiChatReportRepository
import com.mymentalcare.server.application.aichat.reader.TodayAiChatRoomReader
import com.mymentalcare.server.application.aichat.request.CreateAiChatReportRequest
import com.mymentalcare.server.application.aichat.response.AiChatReportReadinessResponse
import com.mymentalcare.server.application.aichat.response.AiChatReportResponse
import com.mymentalcare.server.domain.aichat.AiChatReport
import com.mymentalcare.server.domain.aichat.AiChatReportType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class AiChatReportService(
    private val todayAiChatRoomReader: TodayAiChatRoomReader,
    private val aiChatResponseAssembler: AiChatResponseAssembler,
    private val aiChatReportRepository: AiChatReportRepository,
    private val aiChatReportReadinessDecider: AiChatReportReadinessDecider,
    private val aiChatReportGenerator: AiChatReportGenerator,
) : AiChatReportInputPort {
    @Transactional(readOnly = true)
    override fun readReports(memberId: Long): List<AiChatReportResponse> {
        return aiChatReportRepository.findByMemberId(memberId).map { aiChatResponseAssembler.toReportResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun readReport(memberId: Long, reportId: Long): AiChatReportResponse? {
        return aiChatReportRepository.findByIdAndMemberId(reportId = reportId, memberId = memberId)
            ?.let { aiChatResponseAssembler.toReportResponse(it) }
    }

    // 오늘 대화가 리포트를 만들 만큼 충분한지 판단한다.
    @Transactional(readOnly = true)
    override fun readTodayReportReadiness(memberId: Long): AiChatReportReadinessResponse {
        val room = todayAiChatRoomReader.readTodayRoom(memberId)
        val messages = room?.let { aiChatResponseAssembler.readMessages(it) }.orEmpty()
        val readiness = aiChatReportReadinessDecider.decide(messages)

        return aiChatResponseAssembler.toReportReadinessResponse(readiness)
    }

    // 오늘 대화를 FULL 또는 SHORT 리포트로 정리하고 생성 즉시 저장한다.
    @Transactional
    override fun createTodayReport(memberId: Long, request: CreateAiChatReportRequest): AiChatReportResponse {
        val room = todayAiChatRoomReader.readOrCreateTodayRoom(memberId)
        val existingRequestReport = request.clientRequestId
            ?.let { aiChatReportRepository.findByRoomIdAndClientRequestId(room.id, it) }
        if (existingRequestReport != null) {
            return aiChatResponseAssembler.toReportResponse(existingRequestReport)
        }

        val existingReport = aiChatReportRepository.findLatestByRoomId(room.id)
        if (existingReport != null) {
            return aiChatResponseAssembler.toReportResponse(existingReport)
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

        return aiChatResponseAssembler.toReportResponse(savedReport)
    }

    // 오늘 저장된 최신 마음 리포트를 조회한다.
    @Transactional(readOnly = true)
    override fun readLatestTodayReport(memberId: Long): AiChatReportResponse? {
        val room = todayAiChatRoomReader.readTodayRoom(memberId) ?: return null

        return aiChatReportRepository.findLatestByRoomId(room.id)
            ?.let { aiChatResponseAssembler.toReportResponse(it) }
    }
}
