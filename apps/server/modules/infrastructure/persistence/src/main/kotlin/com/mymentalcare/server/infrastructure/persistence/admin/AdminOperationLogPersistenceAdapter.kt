package com.mymentalcare.server.infrastructure.persistence.admin

import com.mymentalcare.server.application.admin.port.AdminIncidentPage
import com.mymentalcare.server.application.admin.port.AdminOperationLogRepository
import com.mymentalcare.server.domain.admin.AdminIncident
import com.mymentalcare.server.domain.admin.AdminIncidentImpact
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class AdminOperationLogPersistenceAdapter(
    private val jpaAdminIncidentRepository: JpaAdminIncidentRepository,
) : AdminOperationLogRepository {
    override fun findIncidents(
        impact: AdminIncidentImpact?,
        keyword: String?,
        page: Int,
        size: Int,
    ): AdminIncidentPage {
        val result = jpaAdminIncidentRepository.findForAdmin(
            impact = impact,
            keywordLike = keyword?.lowercase()?.let { "%$it%" },
            keywordId = keyword?.toLongOrNull(),
            pageable = PageRequest.of(page, size),
        )

        return AdminIncidentPage(
            incidents = result.content.map { it.toDomain() },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    override fun findIncidentsBetween(startAt: LocalDateTime, endAt: LocalDateTime): List<AdminIncident> {
        return jpaAdminIncidentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
            startAt = startAt,
            endAt = endAt,
        ).map { it.toDomain() }
    }

    override fun saveIncident(incident: AdminIncident): AdminIncident {
        return jpaAdminIncidentRepository.save(incident.toEntity()).toDomain()
    }
}
