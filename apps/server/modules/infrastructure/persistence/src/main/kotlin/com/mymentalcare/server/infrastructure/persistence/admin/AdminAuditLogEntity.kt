package com.mymentalcare.server.infrastructure.persistence.admin

import com.mymentalcare.server.domain.admin.AdminAuditLog
import com.mymentalcare.server.domain.admin.AdminAuditLogAction
import com.mymentalcare.server.domain.admin.AdminAuditLogTargetType
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
@Table(name = "admin_audit_logs")
class AdminAuditLogEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "admin_member_id", nullable = false)
    val adminMemberId: Long,

    @Column(name = "admin_login_id", nullable = false, length = 100)
    val adminLoginId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    val action: AdminAuditLogAction,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 50)
    val targetType: AdminAuditLogTargetType? = null,

    @Column(name = "target_id")
    val targetId: Long? = null,

    @Column(name = "reason", columnDefinition = "TEXT")
    val reason: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): AdminAuditLog {
        return AdminAuditLog(
            id = id,
            adminMemberId = adminMemberId,
            adminLoginId = adminLoginId,
            action = action,
            targetType = targetType,
            targetId = targetId,
            reason = reason,
            createdAt = createdAt,
        )
    }
}

fun AdminAuditLog.toEntity(): AdminAuditLogEntity {
    return AdminAuditLogEntity(
        id = id,
        adminMemberId = adminMemberId,
        adminLoginId = adminLoginId,
        action = action,
        targetType = targetType,
        targetId = targetId,
        reason = reason,
        createdAt = createdAt ?: LocalDateTime.now(),
    )
}
