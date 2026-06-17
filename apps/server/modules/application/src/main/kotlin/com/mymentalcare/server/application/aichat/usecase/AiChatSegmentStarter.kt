package com.mymentalcare.server.application.aichat.usecase

import com.mymentalcare.server.application.aichat.*
import com.mymentalcare.server.application.aichat.policy.*
import com.mymentalcare.server.application.aichat.port.*
import com.mymentalcare.server.application.aichat.reader.*
import com.mymentalcare.server.application.aichat.recorder.*
import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.AiChatSegment
import com.mymentalcare.server.domain.aichat.AiChatSegmentStartType
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.LocalTime

private const val SEGMENT_TITLE_MAX_LENGTH = 120

@Component
internal class AiChatSegmentStarter(
    private val aiChatSegmentRepository: AiChatSegmentRepository,
) {
    // 같은 시작 요청이 반복되어도 오늘 방 안에 중복 구간이 생기지 않게 구간을 시작한다.
    fun startDirectSegment(room: AiChatRoom, clientRequestId: String?): AiChatSegment {
        val existingSegment = readExistingSegmentByClientRequestId(room, clientRequestId)
        if (existingSegment != null) {
            return existingSegment
        }

        return saveNewSegment(
            room = room,
            startType = AiChatSegmentStartType.DIRECT,
            title = "새 주제",
            clientRequestId = clientRequestId,
        )
    }

    // 체크인 요약을 기준으로 오늘 방 안의 새 대화 구간을 시작한다.
    fun startCheckInSegment(
        room: AiChatRoom,
        startType: AiChatSegmentStartType,
        summaryText: String,
        clientRequestId: String?,
    ): AiChatSegment {
        val existingSegment = readExistingSegmentByClientRequestId(room, clientRequestId)
        if (existingSegment != null) {
            return existingSegment
        }

        return saveNewSegment(
            room = room,
            startType = startType,
            title = "${readDayPartLabel()} 체크인 · $summaryText".take(SEGMENT_TITLE_MAX_LENGTH),
            clientRequestId = clientRequestId,
        )
    }

    // 메시지를 보낼 명시 구간이 없으면 마지막 구간을 쓰고, 구간이 전혀 없으면 바로 상담 구간을 만든다.
    fun readActiveSegmentOrStartDirectSegment(room: AiChatRoom, segmentId: Long?): AiChatSegment {
        if (segmentId != null) {
            val segment = aiChatSegmentRepository.findById(segmentId)
                ?: throw AiChatInvalidRequestException("유효하지 않은 대화 구간입니다.")
            if (segment.roomId != room.id) {
                throw AiChatInvalidRequestException("오늘 대화방에 속하지 않은 대화 구간입니다.")
            }
            return segment
        }

        return aiChatSegmentRepository.findLatestByRoomId(room.id)
            ?: startDirectSegment(room, clientRequestId = null)
    }

    private fun saveNewSegment(
        room: AiChatRoom,
        startType: AiChatSegmentStartType,
        title: String,
        clientRequestId: String?,
    ): AiChatSegment {
        return aiChatSegmentRepository.save(
            AiChatSegment(
                id = 0,
                roomId = room.id,
                segmentOrder = aiChatSegmentRepository.countByRoomId(room.id) + 1,
                startType = startType,
                title = title,
                clientRequestId = clientRequestId?.takeIf { it.isNotBlank() },
                startedAt = LocalDateTime.now(),
            )
        )
    }

    private fun readExistingSegmentByClientRequestId(room: AiChatRoom, clientRequestId: String?): AiChatSegment? {
        val requestId = clientRequestId?.takeIf { it.isNotBlank() } ?: return null
        return aiChatSegmentRepository.findByRoomIdAndClientRequestId(room.id, requestId)
    }

    private fun readDayPartLabel(): String {
        val hour = LocalTime.now().hour
        return when {
            hour < 12 -> "오전"
            hour < 18 -> "오후"
            else -> "저녁"
        }
    }
}
