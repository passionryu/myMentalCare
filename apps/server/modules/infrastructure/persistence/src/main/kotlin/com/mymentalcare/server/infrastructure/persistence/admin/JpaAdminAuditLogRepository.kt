package com.mymentalcare.server.infrastructure.persistence.admin

import com.mymentalcare.server.domain.admin.AdminAuditLogTargetType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface JpaAdminAuditLogRepository : JpaRepository<AdminAuditLogEntity, Long> {
    @Query(
        """
        select log
        from AdminAuditLogEntity log
        where (:targetType is null or log.targetType = :targetType)
          and (:targetId is null or log.targetId = :targetId)
        order by log.createdAt desc, log.id desc
        """,
    )
    fun findByTarget(
        @Param("targetType") targetType: AdminAuditLogTargetType?,
        @Param("targetId") targetId: Long?,
        pageable: Pageable,
    ): Page<AdminAuditLogEntity>
}
