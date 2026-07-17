package com.mymentalcare.server.application.admin.usecase

import com.mymentalcare.server.application.admin.port.AdminAuditLogInputPort
import com.mymentalcare.server.application.admin.port.AdminAuditLogPage
import com.mymentalcare.server.application.admin.port.AdminAuditLogRepository
import com.mymentalcare.server.application.admin.request.AdminAuditLogSearchRequest
import com.mymentalcare.server.application.admin.response.AdminAuditLogPageResponse
import com.mymentalcare.server.application.admin.response.AdminAuditLogResponse
import com.mymentalcare.server.domain.admin.AdminAuditLog
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminAuditLogService(
    private val adminAuditLogRepository: AdminAuditLogRepository,
) : AdminAuditLogInputPort {
    @Transactional(readOnly = true)
    override fun readAuditLogs(request: AdminAuditLogSearchRequest): AdminAuditLogPageResponse {
        val normalizedRequest = request.normalized()

        return adminAuditLogRepository.findByTarget(
            targetType = normalizedRequest.targetType,
            targetId = normalizedRequest.targetId,
            page = normalizedRequest.page,
            size = normalizedRequest.size,
        ).toResponse()
    }
}

private fun AdminAuditLogPage.toResponse(): AdminAuditLogPageResponse {
    return AdminAuditLogPageResponse(
        logs = logs.map { it.toResponse() },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
}

private fun AdminAuditLog.toResponse(): AdminAuditLogResponse {
    return AdminAuditLogResponse(
        id = id,
        adminMemberId = adminMemberId,
        adminLoginId = adminLoginId,
        action = action.name,
        targetType = targetType?.name,
        targetId = targetId,
        reason = reason,
        createdAt = createdAt,
    )
}
