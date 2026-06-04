package com.mymentalcare.server.application.member

import com.mymentalcare.server.application.port.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class MemberService(
    private val memberRepository: MemberRepository,
) : MemberInputPort {
    // 내 프로필 조회
    @Transactional(readOnly = true)
    override fun readMyProfile(memberId: Long): MyProfileResponse {
        val member = memberRepository.findById(memberId)
            ?: throw MemberNotFoundException()

        return MyProfileResponse(
            memberId = member.id,
            loginId = member.loginId,
            email = member.email,
            name = member.name,
            phone = member.phone,
        )
    }
}
