package com.mymentalcare.server.application.admin.response

import java.time.LocalDate
import java.time.LocalDateTime

data class AdminOperationLogPageResponse(
    val logs: List<AdminOperationLogResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class AdminOperationLogResponse(
    val id: Long,
    val level: String,
    val title: String,
    val description: String?,
    val action: String?,
    val createdByMemberId: Long,
    val createdAt: LocalDateTime?,
)

data class AdminOperationLogSummaryResponse(
    val from: LocalDate,
    val to: LocalDate,
    val totalIncidents: Int,
    val highImpactIncidents: Int,
    val criticalIncidents: Int,
    val actionRequiredIncidents: Int,
    val latestIncidentAt: LocalDateTime?,
)

data class AdminIncidentCreateResponse(
    val incidentId: Long,
    val createdAt: LocalDateTime?,
)
