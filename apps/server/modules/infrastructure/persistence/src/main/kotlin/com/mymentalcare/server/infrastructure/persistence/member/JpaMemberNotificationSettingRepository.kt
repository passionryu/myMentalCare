package com.mymentalcare.server.infrastructure.persistence.member

import org.springframework.data.jpa.repository.JpaRepository

interface JpaMemberNotificationSettingRepository : JpaRepository<MemberNotificationSettingEntity, Long> {
    fun findByMemberId(memberId: Long): MemberNotificationSettingEntity?
}
