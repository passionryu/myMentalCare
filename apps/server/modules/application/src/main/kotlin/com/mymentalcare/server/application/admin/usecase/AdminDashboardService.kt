package com.mymentalcare.server.application.admin.usecase

import com.mymentalcare.server.application.admin.port.AdminDashboardInputPort
import com.mymentalcare.server.application.admin.port.AdminDashboardQueryRepository
import com.mymentalcare.server.application.admin.response.AdminDashboardAlertResponse
import com.mymentalcare.server.application.admin.response.AdminDashboardDailyMetricResponse
import com.mymentalcare.server.application.admin.response.AdminDashboardSummaryResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

private val DASHBOARD_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")

@Service
class AdminDashboardService(
    private val adminDashboardQueryRepository: AdminDashboardQueryRepository,
) : AdminDashboardInputPort {
    @Transactional(readOnly = true)
    override fun readSummary(): AdminDashboardSummaryResponse {
        val today = today()

        return AdminDashboardSummaryResponse(
            totalMembers = adminDashboardQueryRepository.countActiveMembers(),
            todaySignups = adminDashboardQueryRepository.countMembersCreatedOn(today),
            todayConversations = adminDashboardQueryRepository.countConversationsOn(today),
            todayReports = adminDashboardQueryRepository.countReportsOn(today),
            failedReports = 0,
            baseDate = today,
        )
    }

    @Transactional(readOnly = true)
    override fun readDailyMetrics(days: Int): List<AdminDashboardDailyMetricResponse> {
        val normalizedDays = days.coerceIn(1, 31)
        val startDate = today().minusDays((normalizedDays - 1).toLong())

        return (0 until normalizedDays).map { offset ->
            val date = startDate.plusDays(offset.toLong())

            AdminDashboardDailyMetricResponse(
                date = date,
                signups = adminDashboardQueryRepository.countMembersCreatedOn(date),
                conversations = adminDashboardQueryRepository.countConversationsOn(date),
                reports = adminDashboardQueryRepository.countReportsOn(date),
            )
        }
    }

    @Transactional(readOnly = true)
    override fun readAlerts(): List<AdminDashboardAlertResponse> {
        val summary = readSummary()
        val alerts = mutableListOf<AdminDashboardAlertResponse>()

        if (summary.todayConversations == 0L) {
            alerts += AdminDashboardAlertResponse(
                type = "LOW_ACTIVITY",
                severity = "INFO",
                title = "오늘 대화가 아직 없습니다",
                description = "서비스 유입 또는 로그인 흐름을 한 번 확인하세요.",
                targetPath = "/admin/conversations",
            )
        }

        if (summary.todayReports < summary.todayConversations && summary.todayConversations > 0) {
            alerts += AdminDashboardAlertResponse(
                type = "REPORT_GAP",
                severity = "NOTICE",
                title = "대화 대비 리포트 생성이 적습니다",
                description = "사용자가 대화를 마무리하지 않았거나 리포트 생성 흐름을 이탈했을 수 있습니다.",
                targetPath = "/admin/conversations",
            )
        }

        if (alerts.isEmpty()) {
            alerts += AdminDashboardAlertResponse(
                type = "NORMAL",
                severity = "OK",
                title = "오늘 핵심 흐름은 정상 범위입니다",
                description = "회원, 대화, 리포트 지표에서 즉시 확인할 경고가 없습니다.",
                targetPath = null,
            )
        }

        return alerts
    }

    private fun today(): LocalDate {
        return LocalDate.now(DASHBOARD_ZONE_ID)
    }
}
