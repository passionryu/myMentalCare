package com.mymentalcare.server.application.admin.port

import com.mymentalcare.server.application.admin.request.AdminIncidentCreateRequest
import com.mymentalcare.server.application.admin.request.AdminOperationLogSearchRequest
import com.mymentalcare.server.application.admin.request.AdminOperationLogSummaryRequest
import com.mymentalcare.server.application.admin.response.AdminIncidentCreateResponse
import com.mymentalcare.server.application.admin.response.AdminOperationLogPageResponse
import com.mymentalcare.server.application.admin.response.AdminOperationLogSummaryResponse

interface AdminOperationLogInputPort {
    fun readOperationLogs(request: AdminOperationLogSearchRequest): AdminOperationLogPageResponse

    fun readOperationLogSummary(request: AdminOperationLogSummaryRequest): AdminOperationLogSummaryResponse

    fun createIncident(request: AdminIncidentCreateRequest): AdminIncidentCreateResponse
}
