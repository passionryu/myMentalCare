package com.mymentalcare.server.infrastructure.persistence.aichat

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface JpaAiChatReportRepository : JpaRepository<AiChatReportEntity, Long> {
    fun findTopByRoomIdOrderByCreatedAtDesc(roomId: Long): AiChatReportEntity?

    fun findTopByMemberIdOrderByCreatedAtDesc(memberId: Long): AiChatReportEntity?

    fun countByMemberId(memberId: Long): Int

    fun countByConversationDate(conversationDate: java.time.LocalDate): Long

    fun findByMemberIdOrderByCreatedAtDesc(memberId: Long): List<AiChatReportEntity>

    fun findByMemberIdAndConversationDateBetweenOrderByConversationDateAscCreatedAtDesc(
        memberId: Long,
        startDate: java.time.LocalDate,
        endDate: java.time.LocalDate,
    ): List<AiChatReportEntity>

    fun findByIdAndMemberId(reportId: Long, memberId: Long): AiChatReportEntity?

    fun findByRoomIdAndClientRequestId(roomId: Long, clientRequestId: String): AiChatReportEntity?

    @Query(
        """
        select report
        from AiChatReportEntity report
        where (:memberId is null or report.memberId = :memberId)
          and (:date is null or report.conversationDate = :date)
          and (
            :keywordLike is null
            or lower(report.primaryEmotion) like :keywordLike
            or lower(report.mainCause) like :keywordLike
            or lower(report.summary) like :keywordLike
            or lower(report.todaySentence) like :keywordLike
          )
        order by report.createdAt desc, report.id desc
        """,
    )
    fun findForAdmin(
        @Param("memberId") memberId: Long?,
        @Param("date") date: LocalDate?,
        @Param("keywordLike") keywordLike: String?,
        pageable: Pageable,
    ): Page<AiChatReportEntity>
}

interface JpaAiChatReportSongRepository : JpaRepository<AiChatReportSongEntity, Long> {
    fun findByReportIdOrderBySongOrderAsc(reportId: Long): List<AiChatReportSongEntity>
}

interface JpaAiChatReportEmotionPointRepository : JpaRepository<AiChatReportEmotionPointEntity, Long> {
    fun findByReportIdOrderByPointOrderAsc(reportId: Long): List<AiChatReportEmotionPointEntity>
}
