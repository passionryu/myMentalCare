package com.mymentalcare.server.domain.admin

import java.time.LocalDateTime

data class AdminIncident(
    val id: Long = 0,
    val title: String,
    val description: String?,
    val impact: AdminIncidentImpact,
    val action: String?,
    val createdByMemberId: Long,
    val createdAt: LocalDateTime? = null,
)

enum class AdminIncidentImpact {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
}
