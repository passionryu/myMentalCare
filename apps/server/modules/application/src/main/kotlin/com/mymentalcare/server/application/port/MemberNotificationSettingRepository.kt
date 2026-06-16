package com.mymentalcare.server.application.port

import com.mymentalcare.server.domain.member.MemberNotificationSetting

interface MemberNotificationSettingRepository {
    fun findByMemberId(memberId: Long): MemberNotificationSetting?

    fun save(setting: MemberNotificationSetting): MemberNotificationSetting
}
