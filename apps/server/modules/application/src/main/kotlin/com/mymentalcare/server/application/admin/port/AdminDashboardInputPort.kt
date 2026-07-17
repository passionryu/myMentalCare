package com.mymentalcare.server.application.admin.port

import com.mymentalcare.server.application.admin.response.AdminDashboardAlertResponse
import com.mymentalcare.server.application.admin.response.AdminDashboardDailyMetricResponse
import com.mymentalcare.server.application.admin.response.AdminDashboardSummaryResponse

interface AdminDashboardInputPort {
    fun readSummary(): AdminDashboardSummaryResponse

    fun readDailyMetrics(days: Int): List<AdminDashboardDailyMetricResponse>

    fun readAlerts(): List<AdminDashboardAlertResponse>
}
