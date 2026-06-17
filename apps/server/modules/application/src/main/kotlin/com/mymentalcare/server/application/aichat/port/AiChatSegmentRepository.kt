package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.AiChatSegment

interface AiChatSegmentRepository {
    fun findById(segmentId: Long): AiChatSegment?

    fun findByRoomId(roomId: Long): List<AiChatSegment>

    fun findLatestByRoomId(roomId: Long): AiChatSegment?

    fun findByRoomIdAndClientRequestId(roomId: Long, clientRequestId: String): AiChatSegment?

    fun countByRoomId(roomId: Long): Int

    fun save(segment: AiChatSegment): AiChatSegment
}
