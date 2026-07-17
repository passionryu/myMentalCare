package com.mymentalcare.server.application.admin.request

import com.mymentalcare.server.domain.admin.AdminAuditLogAction
import com.mymentalcare.server.domain.admin.AdminAuditLogTargetType

data class AdminAuditLogRecordRequest(
    val adminMemberId: Long,
    val adminLoginId: String,
    val action: AdminAuditLogAction,
    val targetType: AdminAuditLogTargetType? = null,
    val targetId: Long? = null,
    val reason: String? = null,
)
