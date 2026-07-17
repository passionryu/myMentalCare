package com.mymentalcare.server.infrastructure.persistence.admin

import com.mymentalcare.server.application.admin.port.AdminAuditLogPage
import com.mymentalcare.server.application.admin.port.AdminAuditLogRepository
import com.mymentalcare.server.domain.admin.AdminAuditLog
import com.mymentalcare.server.domain.admin.AdminAuditLogTargetType
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class AdminAuditLogPersistenceAdapter(
    private val jpaAdminAuditLogRepository: JpaAdminAuditLogRepository,
) : AdminAuditLogRepository {
    override fun save(log: AdminAuditLog): AdminAuditLog {
        return jpaAdminAuditLogRepository.save(log.toEntity()).toDomain()
    }

    override fun findByTarget(
        targetType: AdminAuditLogTargetType?,
        targetId: Long?,
        page: Int,
        size: Int,
    ): AdminAuditLogPage {
        val result = jpaAdminAuditLogRepository.findByTarget(
            targetType = targetType,
            targetId = targetId,
            pageable = PageRequest.of(page, size),
        )

        return AdminAuditLogPage(
            logs = result.content.map { it.toDomain() },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }
}
