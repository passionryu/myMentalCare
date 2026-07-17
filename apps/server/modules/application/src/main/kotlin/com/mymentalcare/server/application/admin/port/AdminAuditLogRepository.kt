package com.mymentalcare.server.application.admin.port

import com.mymentalcare.server.domain.admin.AdminAuditLog
import com.mymentalcare.server.domain.admin.AdminAuditLogTargetType

interface AdminAuditLogRepository {
    fun save(log: AdminAuditLog): AdminAuditLog

    fun findByTarget(
        targetType: AdminAuditLogTargetType?,
        targetId: Long?,
        page: Int,
        size: Int,
    ): AdminAuditLogPage
}

data class AdminAuditLogPage(
    val logs: List<AdminAuditLog>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
