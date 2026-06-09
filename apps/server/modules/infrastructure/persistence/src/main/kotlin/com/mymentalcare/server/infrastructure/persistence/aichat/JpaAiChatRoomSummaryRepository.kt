package com.mymentalcare.server.infrastructure.persistence.aichat

import org.springframework.data.jpa.repository.JpaRepository

interface JpaAiChatRoomSummaryRepository : JpaRepository<AiChatRoomSummaryEntity, Long> {
    fun findByRoomId(roomId: Long): AiChatRoomSummaryEntity?
}
