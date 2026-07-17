package com.mymentalcare.server.bootstrap.admin.web

import com.mymentalcare.server.application.admin.port.AdminDashboardInputPort
import com.mymentalcare.server.bootstrap.admin.web.response.AdminDashboardAlertResponse
import com.mymentalcare.server.bootstrap.admin.web.response.AdminDashboardDailyMetricResponse
import com.mymentalcare.server.bootstrap.admin.web.response.AdminDashboardSummaryResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/dashboard")
class AdminDashboardController(
    private val adminDashboardInputPort: AdminDashboardInputPort,
) {
    @Operation(
        summary = "관리자 대시보드 요약 조회",
        description = "관리자 대시보드의 핵심 운영 지표를 서버 기준 Asia/Seoul 날짜로 집계합니다.",
    )
    @GetMapping("/summary")
    fun readSummary(): AdminDashboardSummaryResponse {
        return adminDashboardInputPort.readSummary().toBootstrapResponse()
    }

    @Operation(
        summary = "관리자 대시보드 일자별 지표 조회",
        description = "최근 N일의 가입, 대화, 리포트 생성 추이를 조회합니다. days는 1~31 범위로 보정됩니다.",
    )
    @GetMapping("/daily-metrics")
    fun readDailyMetrics(
        @RequestParam(defaultValue = "7") days: Int,
    ): List<AdminDashboardDailyMetricResponse> {
        return adminDashboardInputPort.readDailyMetrics(days).map { it.toBootstrapResponse() }
    }

    @Operation(
        summary = "관리자 대시보드 운영 알림 조회",
        description = "현재 운영자가 먼저 확인하면 좋은 알림을 조회합니다.",
    )
    @GetMapping("/alerts")
    fun readAlerts(): List<AdminDashboardAlertResponse> {
        return adminDashboardInputPort.readAlerts().map { it.toBootstrapResponse() }
    }
}

private fun com.mymentalcare.server.application.admin.response.AdminDashboardSummaryResponse.toBootstrapResponse(): AdminDashboardSummaryResponse {
    return AdminDashboardSummaryResponse(
        totalMembers = totalMembers,
        todaySignups = todaySignups,
        todayConversations = todayConversations,
        todayReports = todayReports,
        failedReports = failedReports,
        baseDate = baseDate,
    )
}

private fun com.mymentalcare.server.application.admin.response.AdminDashboardDailyMetricResponse.toBootstrapResponse(): AdminDashboardDailyMetricResponse {
    return AdminDashboardDailyMetricResponse(
        date = date,
        signups = signups,
        conversations = conversations,
        reports = reports,
    )
}

private fun com.mymentalcare.server.application.admin.response.AdminDashboardAlertResponse.toBootstrapResponse(): AdminDashboardAlertResponse {
    return AdminDashboardAlertResponse(
        type = type,
        severity = severity,
        title = title,
        description = description,
        targetPath = targetPath,
    )
}
