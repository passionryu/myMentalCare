package com.mymentalcare.server.application.member.request

import java.time.LocalTime

data class MemberNotificationSettingRequest(
    val enabled: Boolean,
    val notificationTime: LocalTime,
    val weekdays: List<String>,
)
