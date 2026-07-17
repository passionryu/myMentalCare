package com.mymentalcare.server.infrastructure.persistence.admin

import com.mymentalcare.server.domain.admin.AdminIncidentImpact
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface JpaAdminIncidentRepository : JpaRepository<AdminIncidentEntity, Long> {
    @Query(
        """
        select incident
        from AdminIncidentEntity incident
        where (:impact is null or incident.impact = :impact)
          and (
            :keywordLike is null
            or lower(incident.title) like :keywordLike
            or lower(coalesce(incident.description, '')) like :keywordLike
            or lower(coalesce(incident.action, '')) like :keywordLike
            or incident.id = :keywordId
          )
        order by incident.createdAt desc, incident.id desc
        """,
    )
    fun findForAdmin(
        @Param("impact") impact: AdminIncidentImpact?,
        @Param("keywordLike") keywordLike: String?,
        @Param("keywordId") keywordId: Long?,
        pageable: Pageable,
    ): Page<AdminIncidentEntity>

    fun findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
        startAt: LocalDateTime,
        endAt: LocalDateTime,
    ): List<AdminIncidentEntity>
}
