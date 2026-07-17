package com.mymentalcare.server.application.admin.request

import com.mymentalcare.server.domain.admin.AdminAuditLogTargetType

data class AdminAuditLogSearchRequest(
    val targetType: AdminAuditLogTargetType? = null,
    val targetId: Long? = null,
    val page: Int = 0,
    val size: Int = 20,
) {
    fun normalized(): AdminAuditLogSearchRequest {
        return copy(
            page = page.coerceAtLeast(0),
            size = size.coerceIn(1, 50),
        )
    }
}
