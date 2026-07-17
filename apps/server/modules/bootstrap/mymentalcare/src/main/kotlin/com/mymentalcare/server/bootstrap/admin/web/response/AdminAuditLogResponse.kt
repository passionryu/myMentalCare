package com.mymentalcare.server.bootstrap.admin.web.response

import java.time.LocalDateTime

data class AdminAuditLogPageResponse(
    val logs: List<AdminAuditLogResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class AdminAuditLogResponse(
    val id: Long,
    val adminMemberId: Long,
    val adminLoginId: String,
    val action: String,
    val targetType: String?,
    val targetId: Long?,
    val reason: String?,
    val createdAt: LocalDateTime?,
)
