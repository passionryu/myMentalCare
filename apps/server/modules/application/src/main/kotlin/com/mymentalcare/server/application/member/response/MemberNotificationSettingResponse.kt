package com.mymentalcare.server.application.member.response

data class MemberNotificationSettingResponse(
    val enabled: Boolean,
    val notificationTime: String,
    val weekdays: List<String>,
)
