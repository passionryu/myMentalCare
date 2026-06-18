package com.mymentalcare.server.application.member.usecase

import com.mymentalcare.server.application.member.MemberNotFoundException
import com.mymentalcare.server.application.member.defaultNotificationSetting
import com.mymentalcare.server.application.member.port.MemberNotificationInputPort
import com.mymentalcare.server.application.member.port.MemberNotificationSettingRepository
import com.mymentalcare.server.application.member.port.MemberRepository
import com.mymentalcare.server.application.member.request.MemberNotificationSettingRequest
import com.mymentalcare.server.application.member.response.MemberNotificationSettingResponse
import com.mymentalcare.server.application.member.toNotificationSettingResponse
import com.mymentalcare.server.domain.member.MemberNotificationSetting
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class MemberNotificationService(
    private val memberRepository: MemberRepository,
    private val notificationSettingRepository: MemberNotificationSettingRepository,
) : MemberNotificationInputPort {
    // 회원의 마음 체크 알림 설정을 조회하고 없으면 기본값을 반환한다.
    @Transactional(readOnly = true)
    override fun readNotificationSetting(memberId: Long): MemberNotificationSettingResponse {
        memberRepository.findById(memberId) ?: throw MemberNotFoundException()

        return (notificationSettingRepository.findByMemberId(memberId) ?: defaultNotificationSetting(memberId))
            .toNotificationSettingResponse()
    }

    // 회원의 마음 체크 알림 사용 여부와 시간을 저장한다.
    @Transactional
    override fun updateNotificationSetting(memberId: Long, request: MemberNotificationSettingRequest): MemberNotificationSettingResponse {
        memberRepository.findById(memberId) ?: throw MemberNotFoundException()

        val currentSetting = notificationSettingRepository.findByMemberId(memberId)
        val savedSetting = notificationSettingRepository.save(
            MemberNotificationSetting(
                id = currentSetting?.id ?: 0,
                memberId = memberId,
                enabled = request.enabled,
                notificationTime = request.notificationTime,
                weekdays = request.weekdays.distinct(),
            )
        )

        return savedSetting.toNotificationSettingResponse()
    }
}
