package com.mymentalcare.server.infrastructure.persistence.member

import com.mymentalcare.server.domain.member.MemberNotificationSetting
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "member_notification_settings")
class MemberNotificationSettingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false, unique = true)
    val memberId: Long,

    @Column(name = "enabled", nullable = false)
    val enabled: Boolean,

    @Column(name = "notification_time", nullable = false)
    val notificationTime: LocalTime,

    @Column(name = "weekdays", nullable = false, length = 40)
    val weekdays: String,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): MemberNotificationSetting {
        return MemberNotificationSetting(
            id = id,
            memberId = memberId,
            enabled = enabled,
            notificationTime = notificationTime,
            weekdays = weekdays.split(",").filter { it.isNotBlank() },
        )
    }
}

fun MemberNotificationSetting.toEntity(): MemberNotificationSettingEntity {
    return MemberNotificationSettingEntity(
        id = id,
        memberId = memberId,
        enabled = enabled,
        notificationTime = notificationTime,
        weekdays = weekdays.joinToString(","),
    )
}
