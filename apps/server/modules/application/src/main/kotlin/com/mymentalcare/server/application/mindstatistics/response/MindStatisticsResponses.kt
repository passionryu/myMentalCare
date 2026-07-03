package com.mymentalcare.server.application.mindstatistics.response

import java.time.LocalDate
import java.time.LocalDateTime

data class MindStatisticsCalendarResponse(
    val month: String,
    val totalConversationDays: Int,
    val totalReportDays: Int,
    val days: List<MindStatisticsCalendarDayResponse>,
)

data class MindStatisticsCalendarDayResponse(
    val date: LocalDate,
    val hasConversation: Boolean,
    val hasReport: Boolean,
    val messageCount: Int,
    val primaryEmotion: String?,
    val emotionIntensity: Int?,
    val todaySentence: String?,
)

data class MindStatisticsDayDetailResponse(
    val date: LocalDate,
    val roomId: Long?,
    val hasConversation: Boolean,
    val messageCount: Int,
    val messages: List<MindStatisticsMessageResponse>,
    val report: MindStatisticsReportResponse?,
)

data class MindStatisticsMessageResponse(
    val messageId: Long,
    val senderType: String,
    val contentPreview: String,
    val messageOrder: Int,
    val isCrisisDetected: Boolean,
    val createdAt: LocalDateTime?,
)

data class MindStatisticsReportResponse(
    val reportId: Long,
    val reportType: String,
    val primaryEmotion: String,
    val emotionIntensity: Int?,
    val mainCause: String,
    val summary: String,
    val emotionalFlow: String,
    val todaySentence: String,
    val createdAt: LocalDateTime?,
)

data class MindStatisticsOverviewResponse(
    val rangeWeeks: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalConversationDays: Int,
    val totalMessages: Int,
    val totalReports: Int,
    val emotionDistribution: List<MindStatisticsEmotionDistributionResponse>,
    val emotionTrend: List<MindStatisticsEmotionTrendResponse>,
)

data class MindStatisticsEmotionDistributionResponse(
    val emotion: String,
    val count: Int,
    val ratio: Double,
)

data class MindStatisticsEmotionTrendResponse(
    val weekStartDate: LocalDate,
    val weekEndDate: LocalDate,
    val averageEmotionIntensity: Double?,
    val reportCount: Int,
)
