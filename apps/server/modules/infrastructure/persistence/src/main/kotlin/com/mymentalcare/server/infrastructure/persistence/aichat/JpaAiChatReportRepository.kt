package com.mymentalcare.server.infrastructure.persistence.aichat

import org.springframework.data.jpa.repository.JpaRepository

interface JpaAiChatReportRepository : JpaRepository<AiChatReportEntity, Long> {
    fun findTopByRoomIdOrderByCreatedAtDesc(roomId: Long): AiChatReportEntity?

    fun findTopByMemberIdOrderByCreatedAtDesc(memberId: Long): AiChatReportEntity?

    fun countByMemberId(memberId: Long): Int

    fun findByMemberIdOrderByCreatedAtDesc(memberId: Long): List<AiChatReportEntity>

    fun findByMemberIdAndConversationDateBetweenOrderByConversationDateAscCreatedAtDesc(
        memberId: Long,
        startDate: java.time.LocalDate,
        endDate: java.time.LocalDate,
    ): List<AiChatReportEntity>

    fun findByIdAndMemberId(reportId: Long, memberId: Long): AiChatReportEntity?

    fun findByRoomIdAndClientRequestId(roomId: Long, clientRequestId: String): AiChatReportEntity?
}

interface JpaAiChatReportSongRepository : JpaRepository<AiChatReportSongEntity, Long> {
    fun findByReportIdOrderBySongOrderAsc(reportId: Long): List<AiChatReportSongEntity>
}

interface JpaAiChatReportEmotionPointRepository : JpaRepository<AiChatReportEmotionPointEntity, Long> {
    fun findByReportIdOrderByPointOrderAsc(reportId: Long): List<AiChatReportEmotionPointEntity>
}
