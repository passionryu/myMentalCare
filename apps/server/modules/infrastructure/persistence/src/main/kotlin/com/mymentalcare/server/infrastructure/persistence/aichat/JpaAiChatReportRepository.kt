package com.mymentalcare.server.infrastructure.persistence.aichat

import org.springframework.data.jpa.repository.JpaRepository

interface JpaAiChatReportRepository : JpaRepository<AiChatReportEntity, Long> {
    fun findTopByRoomIdOrderByCreatedAtDesc(roomId: Long): AiChatReportEntity?

    fun findByRoomIdAndClientRequestId(roomId: Long, clientRequestId: String): AiChatReportEntity?
}

interface JpaAiChatReportSongRepository : JpaRepository<AiChatReportSongEntity, Long> {
    fun findByReportIdOrderBySongOrderAsc(reportId: Long): List<AiChatReportSongEntity>
}
