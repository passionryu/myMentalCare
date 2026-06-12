package com.mymentalcare.server.bootstrap.aichat

import java.time.LocalDate
import java.time.LocalDateTime

data class AiChatReportReadinessResponse(
    val ready: Boolean,
    val reason: String,
    val userMessageCount: Int,
    val userTextLength: Int,
    val requiredUserMessageCount: Int,
    val requiredUserTextLength: Int,
    val unmetRequirements: List<String>,
    val guideMessage: String?,
)

data class AiChatReportResponse(
    val reportId: Long,
    val roomId: Long,
    val reportType: String,
    val conversationDate: LocalDate,
    val summary: String,
    val primaryEmotion: String,
    val emotionIntensity: Int?,
    val mainCause: String,
    val emotionalFlow: String,
    val todaySentence: String,
    val songs: List<AiChatReportSongResponse>,
    val saved: Boolean,
    val createdAt: LocalDateTime?,
)

data class AiChatReportSongResponse(
    val title: String,
    val artist: String,
    val reason: String,
    val youtubeUrl: String,
)
