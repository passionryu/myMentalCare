package com.mymentalcare.server.application.admin.recorder

import com.mymentalcare.server.application.admin.port.AdminAuditLogRepository
import com.mymentalcare.server.application.admin.request.AdminAuditLogRecordRequest
import com.mymentalcare.server.domain.admin.AdminAuditLog
import org.springframework.stereotype.Component

@Component
class AdminAuditLogRecorder(
    private val adminAuditLogRepository: AdminAuditLogRepository,
) {
    fun record(request: AdminAuditLogRecordRequest) {
        adminAuditLogRepository.save(
            AdminAuditLog(
                adminMemberId = request.adminMemberId,
                adminLoginId = request.adminLoginId,
                action = request.action,
                targetType = request.targetType,
                targetId = request.targetId,
                reason = request.reason,
            ),
        )
    }
}
