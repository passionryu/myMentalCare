package com.mymentalcare.server.application.aichat.recorder

import com.mymentalcare.server.application.aichat.policy.*
import com.mymentalcare.server.application.aichat.port.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.AiChatCheckIn
import com.mymentalcare.server.domain.aichat.AiChatCheckInAnswer
import com.mymentalcare.server.domain.aichat.AiChatCheckInTemplateType
import org.springframework.stereotype.Component

@Component
internal class AiChatCheckInRecorder(
    private val aiChatCheckInRepository: AiChatCheckInRepository,
) {
    // 대화 구간 시작 맥락을 잃지 않도록 체크인 원본 답변과 요약을 함께 저장한다.
    fun recordCheckIn(
        segmentId: Long,
        templateType: AiChatCheckInTemplateType,
        answers: List<AiChatCheckInAnswer>,
        summaryText: String,
        detection: CrisisKeywordDetectionResult,
    ): AiChatCheckIn {
        val existingCheckIn = aiChatCheckInRepository.findBySegmentId(segmentId)
        if (existingCheckIn != null) {
            return existingCheckIn
        }

        return aiChatCheckInRepository.save(
            AiChatCheckIn(
                id = 0,
                segmentId = segmentId,
                templateType = templateType,
                answers = answers,
                summaryText = summaryText,
                isCrisisDetected = detection.detected,
                detectedKeywords = detection.keywords,
            )
        )
    }
}
