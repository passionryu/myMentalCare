package com.mymentalcare.server.infrastructure.persistence.aichat

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface JpaChatMessageRepository : JpaRepository<ChatMessageEntity, Long> {
    fun findByRoomIdOrderByMessageOrderAsc(roomId: Long): List<ChatMessageEntity>

    fun findBySegmentIdOrderByMessageOrderAsc(segmentId: Long): List<ChatMessageEntity>

    fun findByRoomIdOrderByMessageOrderDesc(roomId: Long, pageable: Pageable): List<ChatMessageEntity>

    fun findTopByRoomIdOrderByCreatedAtDesc(roomId: Long): ChatMessageEntity?

    fun countByRoomId(roomId: Long): Int
}
