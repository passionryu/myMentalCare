package com.mymentalcare.server.bootstrap.mindstatistics.web

import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsCalendarDayResponse as ApplicationMindStatisticsCalendarDayResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsCalendarResponse as ApplicationMindStatisticsCalendarResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsDayDetailResponse as ApplicationMindStatisticsDayDetailResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsEmotionDistributionResponse as ApplicationMindStatisticsEmotionDistributionResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsEmotionTrendResponse as ApplicationMindStatisticsEmotionTrendResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsMessageResponse as ApplicationMindStatisticsMessageResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsOverviewResponse as ApplicationMindStatisticsOverviewResponse
import com.mymentalcare.server.application.mindstatistics.response.MindStatisticsReportResponse as ApplicationMindStatisticsReportResponse
import com.mymentalcare.server.bootstrap.mindstatistics.web.response.MindStatisticsCalendarDayResponse
import com.mymentalcare.server.bootstrap.mindstatistics.web.response.MindStatisticsCalendarResponse
import com.mymentalcare.server.bootstrap.mindstatistics.web.response.MindStatisticsDayDetailResponse
import com.mymentalcare.server.bootstrap.mindstatistics.web.response.MindStatisticsEmotionDistributionResponse
import com.mymentalcare.server.bootstrap.mindstatistics.web.response.MindStatisticsEmotionTrendResponse
import com.mymentalcare.server.bootstrap.mindstatistics.web.response.MindStatisticsMessageResponse
import com.mymentalcare.server.bootstrap.mindstatistics.web.response.MindStatisticsOverviewResponse
import com.mymentalcare.server.bootstrap.mindstatistics.web.response.MindStatisticsReportResponse

// 애플리케이션 마음 통계 달력 응답을 웹 응답으로 변환한다.
fun ApplicationMindStatisticsCalendarResponse.toBootstrapResponse(): MindStatisticsCalendarResponse {
    return MindStatisticsCalendarResponse(
        month = month,
        totalConversationDays = totalConversationDays,
        totalReportDays = totalReportDays,
        days = days.map { it.toBootstrapResponse() },
    )
}

// 애플리케이션 마음 통계 날짜 상세 응답을 웹 응답으로 변환한다.
fun ApplicationMindStatisticsDayDetailResponse.toBootstrapResponse(): MindStatisticsDayDetailResponse {
    return MindStatisticsDayDetailResponse(
        date = date,
        roomId = roomId,
        hasConversation = hasConversation,
        messageCount = messageCount,
        messages = messages.map { it.toBootstrapResponse() },
        report = report?.toBootstrapResponse(),
    )
}

// 애플리케이션 마음 통계 요약 응답을 웹 응답으로 변환한다.
fun ApplicationMindStatisticsOverviewResponse.toBootstrapResponse(): MindStatisticsOverviewResponse {
    return MindStatisticsOverviewResponse(
        rangeWeeks = rangeWeeks,
        startDate = startDate,
        endDate = endDate,
        totalConversationDays = totalConversationDays,
        totalMessages = totalMessages,
        totalReports = totalReports,
        emotionDistribution = emotionDistribution.map { it.toBootstrapResponse() },
        emotionTrend = emotionTrend.map { it.toBootstrapResponse() },
    )
}

// 애플리케이션 달력 날짜 응답을 웹 응답으로 변환한다.
private fun ApplicationMindStatisticsCalendarDayResponse.toBootstrapResponse(): MindStatisticsCalendarDayResponse {
    return MindStatisticsCalendarDayResponse(
        date = date,
        hasConversation = hasConversation,
        hasReport = hasReport,
        messageCount = messageCount,
        primaryEmotion = primaryEmotion,
        emotionIntensity = emotionIntensity,
        todaySentence = todaySentence,
    )
}

// 애플리케이션 메시지 응답을 웹 응답으로 변환한다.
private fun ApplicationMindStatisticsMessageResponse.toBootstrapResponse(): MindStatisticsMessageResponse {
    return MindStatisticsMessageResponse(
        messageId = messageId,
        senderType = senderType,
        contentPreview = contentPreview,
        messageOrder = messageOrder,
        isCrisisDetected = isCrisisDetected,
        createdAt = createdAt,
    )
}

// 애플리케이션 리포트 응답을 웹 응답으로 변환한다.
private fun ApplicationMindStatisticsReportResponse.toBootstrapResponse(): MindStatisticsReportResponse {
    return MindStatisticsReportResponse(
        reportId = reportId,
        reportType = reportType,
        primaryEmotion = primaryEmotion,
        emotionIntensity = emotionIntensity,
        mainCause = mainCause,
        summary = summary,
        emotionalFlow = emotionalFlow,
        todaySentence = todaySentence,
        createdAt = createdAt,
    )
}

// 애플리케이션 감정 분포 응답을 웹 응답으로 변환한다.
private fun ApplicationMindStatisticsEmotionDistributionResponse.toBootstrapResponse(): MindStatisticsEmotionDistributionResponse {
    return MindStatisticsEmotionDistributionResponse(
        emotion = emotion,
        count = count,
        ratio = ratio,
    )
}

// 애플리케이션 감정 추세 응답을 웹 응답으로 변환한다.
private fun ApplicationMindStatisticsEmotionTrendResponse.toBootstrapResponse(): MindStatisticsEmotionTrendResponse {
    return MindStatisticsEmotionTrendResponse(
        weekStartDate = weekStartDate,
        weekEndDate = weekEndDate,
        averageEmotionIntensity = averageEmotionIntensity,
        reportCount = reportCount,
    )
}
