package com.mymentalcare.server.bootstrap.member.web.response

data class MemberNotificationSettingResponse(
    val enabled: Boolean,
    val notificationTime: String,
    val weekdays: List<String>,
)
