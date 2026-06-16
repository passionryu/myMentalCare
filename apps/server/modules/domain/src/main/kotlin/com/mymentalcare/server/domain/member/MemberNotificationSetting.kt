package com.mymentalcare.server.domain.member

import java.time.LocalTime

data class MemberNotificationSetting(
    val id: Long,
    val memberId: Long,
    val enabled: Boolean,
    val notificationTime: LocalTime,
    val weekdays: List<String>,
)
