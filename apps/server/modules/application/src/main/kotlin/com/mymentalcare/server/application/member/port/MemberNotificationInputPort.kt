package com.mymentalcare.server.application.member.port

import com.mymentalcare.server.application.member.request.MemberNotificationSettingRequest
import com.mymentalcare.server.application.member.response.MemberNotificationSettingResponse

interface MemberNotificationInputPort {
    fun readNotificationSetting(memberId: Long): MemberNotificationSettingResponse

    fun updateNotificationSetting(memberId: Long, request: MemberNotificationSettingRequest): MemberNotificationSettingResponse
}
