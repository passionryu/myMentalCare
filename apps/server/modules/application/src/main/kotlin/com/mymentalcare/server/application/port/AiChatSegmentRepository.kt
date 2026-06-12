package com.mymentalcare.server.application.port

import com.mymentalcare.server.domain.aichat.AiChatSegment

interface AiChatSegmentRepository {
    fun findById(segmentId: Long): AiChatSegment?

    fun findByRoomId(roomId: Long): List<AiChatSegment>

    fun findLatestByRoomId(roomId: Long): AiChatSegment?

    fun findByRoomIdAndClientRequestId(roomId: Long, clientRequestId: String): AiChatSegment?

    fun countByRoomId(roomId: Long): Int

    fun save(segment: AiChatSegment): AiChatSegment
}
