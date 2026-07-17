package com.mymentalcare.server.infrastructure.persistence.admin

import com.mymentalcare.server.domain.admin.AdminIncident
import com.mymentalcare.server.domain.admin.AdminIncidentImpact
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "admin_incidents")
class AdminIncidentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "title", nullable = false, length = 160)
    val title: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "impact", nullable = false, length = 30)
    val impact: AdminIncidentImpact,

    @Column(name = "action", columnDefinition = "TEXT")
    val action: String?,

    @Column(name = "created_by_member_id", nullable = false)
    val createdByMemberId: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): AdminIncident {
        return AdminIncident(
            id = id,
            title = title,
            description = description,
            impact = impact,
            action = action,
            createdByMemberId = createdByMemberId,
            createdAt = createdAt,
        )
    }
}

fun AdminIncident.toEntity(): AdminIncidentEntity {
    return AdminIncidentEntity(
        id = id,
        title = title,
        description = description,
        impact = impact,
        action = action,
        createdByMemberId = createdByMemberId,
        createdAt = createdAt ?: LocalDateTime.now(),
    )
}
