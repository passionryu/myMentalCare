package com.mymentalcare.server.infrastructure.persistence.aichat

import org.springframework.data.jpa.repository.JpaRepository

interface JpaAiChatCheckInRepository : JpaRepository<AiChatCheckInEntity, Long> {
    fun findBySegmentId(segmentId: Long): AiChatCheckInEntity?

    fun findBySegmentIdIn(segmentIds: List<Long>): List<AiChatCheckInEntity>
}
