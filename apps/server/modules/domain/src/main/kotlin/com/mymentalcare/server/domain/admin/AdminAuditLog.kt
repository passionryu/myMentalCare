package com.mymentalcare.server.domain.admin

import java.time.LocalDateTime

data class AdminAuditLog(
    val id: Long = 0,
    val adminMemberId: Long,
    val adminLoginId: String,
    val action: AdminAuditLogAction,
    val targetType: AdminAuditLogTargetType? = null,
    val targetId: Long? = null,
    val reason: String? = null,
    val createdAt: LocalDateTime? = null,
)
