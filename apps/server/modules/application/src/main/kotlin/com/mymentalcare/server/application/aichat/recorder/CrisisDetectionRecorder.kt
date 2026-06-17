package com.mymentalcare.server.application.aichat.recorder

import com.mymentalcare.server.application.aichat.policy.*
import com.mymentalcare.server.application.aichat.port.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.AiChatCheckIn
import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.CrisisDetectionEvent
import com.mymentalcare.server.domain.aichat.CrisisDetectionSourceType
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
                sourceType = CrisisDetectionSourceType.MESSAGE,
                messageId = message.id,
                checkInId = null,
                detectedKeywords = detection.keywords,
                riskLevel = detection.riskLevel,
                handledAction = CrisisHandledAction.SAFETY_MODAL_SHOWN,
            )
        )
    }

    // 체크인 직접 입력에서 감지된 위기 표현도 메시지와 구분해 추적 가능하게 기록한다.
    fun recordCheckInSafetyModalShown(
        memberId: Long,
        room: AiChatRoom,
        checkIn: AiChatCheckIn,
        detection: CrisisKeywordDetectionResult,
    ) {
        crisisDetectionEventRepository.save(
            CrisisDetectionEvent(
                id = 0,
                memberId = memberId,
                roomId = room.id,
                sourceType = CrisisDetectionSourceType.CHECK_IN,
                messageId = null,
                checkInId = checkIn.id,
                detectedKeywords = detection.keywords,
                riskLevel = detection.riskLevel,
                handledAction = CrisisHandledAction.SAFETY_MODAL_SHOWN,
            )
        )
    }
}
