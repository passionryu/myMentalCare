package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.application.port.AiChatCheckInRepository
import com.mymentalcare.server.domain.aichat.AiChatSegment
import org.springframework.stereotype.Component

@Component
internal class AiChatSegmentContextReader(
    private val aiChatCheckInRepository: AiChatCheckInRepository,
) {
    // 현재 구간의 목적을 AI 응답 생성기가 참고할 짧은 컨텍스트로 만든다.
    fun readSegmentContext(segment: AiChatSegment): AiChatSegmentContext {
        return AiChatSegmentContext(
            title = segment.title,
            startType = segment.startType.name,
        )
    }

    // 체크인으로 시작한 구간이면 체크인 요약을 AI 응답 컨텍스트에 포함한다.
    fun readCheckInContext(segment: AiChatSegment): AiChatCheckInContext? {
        val checkIn = aiChatCheckInRepository.findBySegmentId(segment.id) ?: return null
        return AiChatCheckInContext(
            templateType = checkIn.templateType.name,
            summaryText = checkIn.summaryText,
        )
    }
}
