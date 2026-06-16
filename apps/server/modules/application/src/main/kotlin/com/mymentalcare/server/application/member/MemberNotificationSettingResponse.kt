package com.mymentalcare.server.application.member

data class MemberNotificationSettingResponse(
    val enabled: Boolean,
    val notificationTime: String,
    val weekdays: List<String>,
)
