package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.application.port.CrisisDetectionEventRepository
import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.CrisisDetectionEvent
import com.mymentalcare.server.domain.aichat.CrisisHandledAction
import org.springframework.stereotype.Component

@Component
internal class CrisisDetectionRecorder(
    private val crisisDetectionEventRepository: CrisisDetectionEventRepository,
) {
    // 위기 표현이 감지된 메시지를 사람이 추적할 수 있도록 기록한다.
    fun recordSafetyModalShown(
        memberId: Long,
        room: AiChatRoom,
        message: ChatMessage,
        detection: CrisisKeywordDetectionResult,
    ) {
        crisisDetectionEventRepository.save(
            CrisisDetectionEvent(
                id = 0,
                memberId = memberId,
                roomId = room.id,
                messageId = message.id,
                detectedKeywords = detection.keywords,
                riskLevel = detection.riskLevel,
                handledAction = CrisisHandledAction.SAFETY_MODAL_SHOWN,
            )
        )
    }
}
