package com.mymentalcare.server.infrastructure.persistence.member

import com.mymentalcare.server.application.port.MemberNotificationSettingRepository
import com.mymentalcare.server.domain.member.MemberNotificationSetting
import org.springframework.stereotype.Repository

@Repository
class MemberNotificationSettingPersistenceAdapter(
    private val jpaMemberNotificationSettingRepository: JpaMemberNotificationSettingRepository,
) : MemberNotificationSettingRepository {
    override fun findByMemberId(memberId: Long): MemberNotificationSetting? {
        return jpaMemberNotificationSettingRepository.findByMemberId(memberId)?.toDomain()
    }

    override fun save(setting: MemberNotificationSetting): MemberNotificationSetting {
        return jpaMemberNotificationSettingRepository.save(setting.toEntity()).toDomain()
    }
}
