package com.mymentalcare.server.application.admin.port

import com.mymentalcare.server.domain.admin.AdminIncident
import com.mymentalcare.server.domain.admin.AdminIncidentImpact
import java.time.LocalDateTime

interface AdminOperationLogRepository {
    fun findIncidents(impact: AdminIncidentImpact?, keyword: String?, page: Int, size: Int): AdminIncidentPage

    fun findIncidentsBetween(startAt: LocalDateTime, endAt: LocalDateTime): List<AdminIncident>

    fun saveIncident(incident: AdminIncident): AdminIncident
}

data class AdminIncidentPage(
    val incidents: List<AdminIncident>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
