package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.CreateAiChatReportRequest
import com.mymentalcare.server.application.aichat.response.AiChatReportReadinessResponse
import com.mymentalcare.server.application.aichat.response.AiChatReportResponse

interface AiChatReportInputPort {
    fun readReports(memberId: Long): List<AiChatReportResponse>

    fun readReport(memberId: Long, reportId: Long): AiChatReportResponse?

    fun readTodayReportReadiness(memberId: Long): AiChatReportReadinessResponse

    fun createTodayReport(memberId: Long, request: CreateAiChatReportRequest): AiChatReportResponse

    fun readLatestTodayReport(memberId: Long): AiChatReportResponse?
}
