package com.mymentalcare.server.application.admin.response

import java.time.LocalDate

data class AdminDashboardSummaryResponse(
    val totalMembers: Long,
    val todaySignups: Long,
    val todayConversations: Long,
    val todayReports: Long,
    val failedReports: Long,
    val baseDate: LocalDate,
)

data class AdminDashboardDailyMetricResponse(
    val date: LocalDate,
    val signups: Long,
    val conversations: Long,
    val reports: Long,
)

data class AdminDashboardAlertResponse(
    val type: String,
    val severity: String,
    val title: String,
    val description: String,
    val targetPath: String?,
)
