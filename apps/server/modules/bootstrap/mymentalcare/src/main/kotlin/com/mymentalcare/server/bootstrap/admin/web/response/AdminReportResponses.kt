package com.mymentalcare.server.bootstrap.admin.web.response

import java.time.LocalDate
import java.time.LocalDateTime

data class AdminReportPageResponse(
    val reports: List<AdminReportSummaryResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class AdminReportSummaryResponse(
    val id: Long,
    val roomId: Long,
    val memberId: Long,
    val conversationDate: LocalDate,
    val primaryEmotion: String,
    val emotionScore: Int?,
    val mainCause: String,
    val createdAt: LocalDateTime?,
)

data class AdminReportDetailResponse(
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
    val songs: List<AdminReportSongResponse>,
    val emotionTimeline: List<AdminReportEmotionPointResponse>,
    val createdAt: LocalDateTime?,
)

data class AdminReportSongResponse(
    val title: String,
    val artist: String,
    val reason: String,
    val youtubeUrl: String,
)

data class AdminReportEmotionPointResponse(
    val label: String,
    val score: Int,
    val reason: String,
)

data class AdminChatMessageResponse(
    val id: Long,
    val roomId: Long,
    val senderType: String,
    val content: String,
    val messageOrder: Int,
    val createdAt: LocalDateTime?,
)
