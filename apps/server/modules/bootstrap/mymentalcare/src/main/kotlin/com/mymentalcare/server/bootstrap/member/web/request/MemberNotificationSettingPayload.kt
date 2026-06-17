package com.mymentalcare.server.bootstrap.member.web.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class MemberNotificationSettingPayload(
    val enabled: Boolean,

    @field:NotBlank(message = "알림 시간을 입력해주세요.")
    @field:Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "알림 시간은 HH:mm 형식으로 입력해주세요.")
    val notificationTime: String,

    @field:Size(min = 1, max = 7, message = "알림 요일을 1개 이상 선택해주세요.")
    val weekdays: List<@Pattern(regexp = "MON|TUE|WED|THU|FRI|SAT|SUN", message = "알림 요일 형식을 확인해주세요.") String>,
)
