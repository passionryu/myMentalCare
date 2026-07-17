package com.mymentalcare.server.bootstrap.admin.web

import com.mymentalcare.server.application.admin.port.AdminAuditLogInputPort
import com.mymentalcare.server.application.admin.request.AdminAuditLogSearchRequest
import com.mymentalcare.server.bootstrap.admin.web.response.AdminAuditLogPageResponse
import com.mymentalcare.server.domain.admin.AdminAuditLogTargetType
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/audit-logs")
class AdminAuditLogController(
    private val adminAuditLogInputPort: AdminAuditLogInputPort,
) {
    @Operation(
        summary = "관리자 감사 로그 조회",
        description = "관리자 콘솔에서 주요 조회/변경 액션의 감사 로그를 조회합니다.",
    )
    @GetMapping
    fun readAuditLogs(
        @RequestParam(required = false) targetType: AdminAuditLogTargetType?,
        @RequestParam(required = false) targetId: Long?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): AdminAuditLogPageResponse {
        return adminAuditLogInputPort.readAuditLogs(
            AdminAuditLogSearchRequest(
                targetType = targetType,
                targetId = targetId,
                page = page,
                size = size,
            ),
        ).toBootstrapResponse()
    }
}

private fun com.mymentalcare.server.application.admin.response.AdminAuditLogPageResponse.toBootstrapResponse(): AdminAuditLogPageResponse {
    return AdminAuditLogPageResponse(
        logs = logs.map {
            com.mymentalcare.server.bootstrap.admin.web.response.AdminAuditLogResponse(
                id = it.id,
                adminMemberId = it.adminMemberId,
                adminLoginId = it.adminLoginId,
                action = it.action,
                targetType = it.targetType,
                targetId = it.targetId,
                reason = it.reason,
                createdAt = it.createdAt,
            )
        },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
}
