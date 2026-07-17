package com.mymentalcare.server.infrastructure.persistence.admin

import com.mymentalcare.server.application.admin.port.AdminChatMessageRecord
import com.mymentalcare.server.application.admin.port.AdminChatRoomRecord
import com.mymentalcare.server.application.admin.port.AdminReportEmotionPointRecord
import com.mymentalcare.server.application.admin.port.AdminReportPage
import com.mymentalcare.server.application.admin.port.AdminReportRecord
import com.mymentalcare.server.application.admin.port.AdminReportRepository
import com.mymentalcare.server.application.admin.port.AdminReportSongRecord
import com.mymentalcare.server.infrastructure.persistence.aichat.AiChatReportEntity
import com.mymentalcare.server.infrastructure.persistence.aichat.JpaAiChatReportEmotionPointRepository
import com.mymentalcare.server.infrastructure.persistence.aichat.JpaAiChatReportRepository
import com.mymentalcare.server.infrastructure.persistence.aichat.JpaAiChatReportSongRepository
import com.mymentalcare.server.infrastructure.persistence.aichat.JpaAiChatRoomRepository
import com.mymentalcare.server.infrastructure.persistence.aichat.JpaChatMessageRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AdminReportPersistenceAdapter(
    private val jpaAiChatReportRepository: JpaAiChatReportRepository,
    private val jpaAiChatReportSongRepository: JpaAiChatReportSongRepository,
    private val jpaAiChatReportEmotionPointRepository: JpaAiChatReportEmotionPointRepository,
    private val jpaAiChatRoomRepository: JpaAiChatRoomRepository,
    private val jpaChatMessageRepository: JpaChatMessageRepository,
) : AdminReportRepository {
    override fun findReports(
        memberId: Long?,
        date: LocalDate?,
        keyword: String?,
        page: Int,
        size: Int,
    ): AdminReportPage {
        val result = jpaAiChatReportRepository.findForAdmin(
            memberId = memberId,
            date = date,
            keywordLike = keyword?.lowercase()?.let { "%$it%" },
            pageable = PageRequest.of(page, size),
        )

        return AdminReportPage(
            reports = result.content.map { it.toAdminReportRecord() },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    override fun findReportById(reportId: Long): AdminReportRecord? {
        return jpaAiChatReportRepository.findById(reportId)
            .map { it.toAdminReportRecord() }
            .orElse(null)
    }

    override fun findChatRoom(roomId: Long): AdminChatRoomRecord? {
        return jpaAiChatRoomRepository.findById(roomId)
            .map {
                AdminChatRoomRecord(
                    id = it.id,
                    memberId = it.memberId,
                    conversationDate = it.conversationDate,
                )
            }
            .orElse(null)
    }

    override fun findMessagesByRoomId(roomId: Long): List<AdminChatMessageRecord> {
        return jpaChatMessageRepository.findByRoomIdOrderByMessageOrderAsc(roomId).map {
            AdminChatMessageRecord(
                id = it.id,
                roomId = it.roomId,
                senderType = it.senderType,
                content = it.content,
                messageOrder = it.messageOrder,
                createdAt = it.createdAt,
            )
        }
    }

    private fun AiChatReportEntity.toAdminReportRecord(): AdminReportRecord {
        return AdminReportRecord(
            id = id,
            roomId = roomId,
            memberId = memberId,
            conversationDate = conversationDate,
            reportType = reportType.name,
            summary = summary,
            primaryEmotion = primaryEmotion,
            emotionIntensity = emotionIntensity,
            emotionScore = emotionScore,
            mainCause = mainCause,
            emotionalFlow = emotionalFlow,
            todaySentence = todaySentence,
            songs = jpaAiChatReportSongRepository.findByReportIdOrderBySongOrderAsc(id).map {
                AdminReportSongRecord(
                    title = it.title,
                    artist = it.artist,
                    reason = it.reason,
                    youtubeUrl = it.youtubeUrl,
                )
            },
            emotionTimeline = jpaAiChatReportEmotionPointRepository.findByReportIdOrderByPointOrderAsc(id).map {
                AdminReportEmotionPointRecord(
                    label = it.label,
                    score = it.score,
                    reason = it.reason,
                )
            },
            createdAt = createdAt,
        )
    }
}
