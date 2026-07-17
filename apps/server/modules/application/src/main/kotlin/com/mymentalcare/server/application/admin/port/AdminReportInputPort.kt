package com.mymentalcare.server.application.admin.port

import com.mymentalcare.server.application.admin.request.AdminChatMessageSearchRequest
import com.mymentalcare.server.application.admin.request.AdminReportSearchRequest
import com.mymentalcare.server.application.admin.response.AdminChatMessageResponse
import com.mymentalcare.server.application.admin.response.AdminReportDetailResponse
import com.mymentalcare.server.application.admin.response.AdminReportPageResponse

interface AdminReportQueryInputPort {
    fun readReports(request: AdminReportSearchRequest): AdminReportPageResponse

    fun readReport(adminMemberId: Long, reportId: Long): AdminReportDetailResponse

    fun readChatMessages(request: AdminChatMessageSearchRequest): List<AdminChatMessageResponse>
}
