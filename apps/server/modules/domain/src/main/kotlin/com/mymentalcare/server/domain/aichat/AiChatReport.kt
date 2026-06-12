package com.mymentalcare.server.domain.aichat

import java.time.LocalDate
import java.time.LocalDateTime

data class AiChatReport(
    val id: Long,
    val roomId: Long,
    val memberId: Long,
    val conversationDate: LocalDate,
    val reportType: AiChatReportType,
    val summary: String,
    val primaryEmotion: String,
    val emotionIntensity: Int?,
    val mainCause: String,
    val emotionalFlow: String,
    val todaySentence: String,
    val clientRequestId: String?,
    val songs: List<AiChatReportSong>,
    val createdAt: LocalDateTime? = null,
)

data class AiChatReportSong(
    val id: Long = 0,
    val reportId: Long = 0,
    val songOrder: Int,
    val title: String,
    val artist: String,
    val reason: String,
    val youtubeUrl: String,
)

enum class AiChatReportType {
    FULL,
    SHORT,
}
