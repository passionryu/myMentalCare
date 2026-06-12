package com.mymentalcare.server.infrastructure.persistence.aichat

import org.springframework.data.jpa.repository.JpaRepository

interface JpaAiChatSegmentRepository : JpaRepository<AiChatSegmentEntity, Long> {
    fun findByRoomIdOrderBySegmentOrderAsc(roomId: Long): List<AiChatSegmentEntity>

    fun findFirstByRoomIdOrderBySegmentOrderDesc(roomId: Long): AiChatSegmentEntity?

    fun findByRoomIdAndClientRequestId(roomId: Long, clientRequestId: String): AiChatSegmentEntity?

    fun countByRoomId(roomId: Long): Int
}
