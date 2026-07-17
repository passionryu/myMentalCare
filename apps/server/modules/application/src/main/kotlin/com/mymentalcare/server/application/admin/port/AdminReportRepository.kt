package com.mymentalcare.server.application.admin.port

import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import java.time.LocalDate
import java.time.LocalDateTime

interface AdminReportRepository {
    fun findReports(memberId: Long?, date: LocalDate?, keyword: String?, page: Int, size: Int): AdminReportPage

    fun findReportById(reportId: Long): AdminReportRecord?

    fun findChatRoom(roomId: Long): AdminChatRoomRecord?

    fun findMessagesByRoomId(roomId: Long): List<AdminChatMessageRecord>
}

data class AdminReportPage(
    val reports: List<AdminReportRecord>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class AdminReportRecord(
    val id: Long,
    val roomId: Long,
    val memberId: Long,
    val conversationDate: LocalDate,
    val reportType: String,
    val summary: String,
    val primaryEmotion: String,
    val emotionIntensity: Int?,
    val emotionScore: Int?,
    val mainCause: String,
    val emotionalFlow: String,
    val todaySentence: String,
    val songs: List<AdminReportSongRecord>,
    val emotionTimeline: List<AdminReportEmotionPointRecord>,
    val createdAt: LocalDateTime?,
)

data class AdminReportSongRecord(
    val title: String,
    val artist: String,
    val reason: String,
    val youtubeUrl: String,
)

data class AdminReportEmotionPointRecord(
    val label: String,
    val score: Int,
    val reason: String,
)

data class AdminChatRoomRecord(
    val id: Long,
    val memberId: Long,
    val conversationDate: LocalDate,
)

data class AdminChatMessageRecord(
    val id: Long,
    val roomId: Long,
    val senderType: ChatMessageSenderType,
    val content: String,
    val messageOrder: Int,
    val createdAt: LocalDateTime?,
)
