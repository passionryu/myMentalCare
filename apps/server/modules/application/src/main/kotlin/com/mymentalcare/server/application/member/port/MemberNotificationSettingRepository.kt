package com.mymentalcare.server.application.member.port

import com.mymentalcare.server.application.member.request.*
import com.mymentalcare.server.application.member.response.*

import com.mymentalcare.server.domain.member.MemberNotificationSetting

interface MemberNotificationSettingRepository {
    fun findByMemberId(memberId: Long): MemberNotificationSetting?

    fun save(setting: MemberNotificationSetting): MemberNotificationSetting
}
