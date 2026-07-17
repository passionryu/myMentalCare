package com.mymentalcare.server.application.admin.port

import com.mymentalcare.server.application.admin.request.AdminAuditLogSearchRequest
import com.mymentalcare.server.application.admin.response.AdminAuditLogPageResponse

interface AdminAuditLogInputPort {
    fun readAuditLogs(request: AdminAuditLogSearchRequest): AdminAuditLogPageResponse
}
