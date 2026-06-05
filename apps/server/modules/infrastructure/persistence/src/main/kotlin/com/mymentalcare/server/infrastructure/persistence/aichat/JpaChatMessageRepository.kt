package com.mymentalcare.server.infrastructure.persistence.aichat

import org.springframework.data.jpa.repository.JpaRepository

interface JpaChatMessageRepository : JpaRepository<ChatMessageEntity, Long> {
    fun findByRoomIdOrderByMessageOrderAsc(roomId: Long): List<ChatMessageEntity>

    fun countByRoomId(roomId: Long): Int
}
