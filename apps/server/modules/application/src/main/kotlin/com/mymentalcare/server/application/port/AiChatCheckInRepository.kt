package com.mymentalcare.server.application.port

import com.mymentalcare.server.domain.aichat.AiChatCheckIn

interface AiChatCheckInRepository {
    fun findBySegmentId(segmentId: Long): AiChatCheckIn?

    fun findBySegmentIds(segmentIds: List<Long>): List<AiChatCheckIn>

    fun save(checkIn: AiChatCheckIn): AiChatCheckIn
}
