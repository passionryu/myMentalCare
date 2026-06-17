package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.AiChatCheckIn

interface AiChatCheckInRepository {
    fun findBySegmentId(segmentId: Long): AiChatCheckIn?

    fun findBySegmentIds(segmentIds: List<Long>): List<AiChatCheckIn>

    fun save(checkIn: AiChatCheckIn): AiChatCheckIn
}
