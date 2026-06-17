package com.mymentalcare.server.application.mypage.response

import java.time.LocalDate
import java.time.LocalDateTime

data class MyPageSummaryResponse(
    val hasTodayChat: Boolean,
    val todayMessageCount: Int,
    val recentChatAt: LocalDateTime?,
    val reportCount: Int,
    val latestReportAt: LocalDateTime?,
    val latestReportDate: LocalDate?,
    val notificationEnabled: Boolean,
    val notificationTime: String,
)
