package com.mymentalcare.server.application.member

import com.mymentalcare.server.application.member.response.MemberNotificationSettingResponse
import com.mymentalcare.server.application.member.response.MyProfileResponse
import com.mymentalcare.server.domain.auth.SocialAccount
import com.mymentalcare.server.domain.member.Member
import com.mymentalcare.server.domain.member.MemberNotificationSetting
import java.time.LocalTime

internal fun Member.toMyProfileResponse(): MyProfileResponse {
    return MyProfileResponse(
        memberId = id,
        loginId = loginId,
        email = email,
        name = name,
        phone = phone,
    )
}

internal fun MemberNotificationSetting.toNotificationSettingResponse(): MemberNotificationSettingResponse {
    return MemberNotificationSettingResponse(
        enabled = enabled,
        notificationTime = notificationTime.toString(),
        weekdays = weekdays,
    )
}

internal fun defaultNotificationSetting(memberId: Long): MemberNotificationSetting {
    return MemberNotificationSetting(
        id = 0,
        memberId = memberId,
        enabled = false,
        notificationTime = LocalTime.of(21, 0),
        weekdays = listOf("MON", "TUE", "WED", "THU", "FRI"),
    )
}

internal fun String?.normalizeBlank(): String? = this?.trim()?.takeIf { it.isNotBlank() }

internal fun Member.isPasswordLoginEnabled(socialAccounts: List<SocialAccount>): Boolean {
    return socialAccounts.isEmpty() || !loginId.startsWith("kakao_")
}
