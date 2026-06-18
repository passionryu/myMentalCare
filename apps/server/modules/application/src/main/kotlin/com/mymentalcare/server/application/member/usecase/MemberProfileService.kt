package com.mymentalcare.server.application.member.usecase

import com.mymentalcare.server.application.member.DuplicateEmailException
import com.mymentalcare.server.application.member.MemberNotFoundException
import com.mymentalcare.server.application.member.normalizeBlank
import com.mymentalcare.server.application.member.port.MemberProfileInputPort
import com.mymentalcare.server.application.member.port.MemberRepository
import com.mymentalcare.server.application.member.request.UpdateMyProfileRequest
import com.mymentalcare.server.application.member.response.MyProfileResponse
import com.mymentalcare.server.application.member.toMyProfileResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class MemberProfileService(
    private val memberRepository: MemberRepository,
) : MemberProfileInputPort {
    // 로그인한 회원의 프로필 정보를 조회한다.
    @Transactional(readOnly = true)
    override fun readMyProfile(memberId: Long): MyProfileResponse {
        val member = memberRepository.findById(memberId)
            ?: throw MemberNotFoundException()

        return member.toMyProfileResponse()
    }

    // 이메일 중복을 검증한 뒤 회원 프로필을 수정한다.
    @Transactional
    override fun updateMyProfile(memberId: Long, request: UpdateMyProfileRequest): MyProfileResponse {
        val member = memberRepository.findById(memberId)
            ?: throw MemberNotFoundException()

        val nextEmail = request.email.normalizeBlank()
        if (nextEmail != null) {
            val emailOwner = memberRepository.findByEmail(nextEmail)
            if (emailOwner != null && emailOwner.id != memberId) {
                throw DuplicateEmailException()
            }
        }

        val updatedMember = memberRepository.save(
            member.copy(
                email = nextEmail,
                name = request.name.trim(),
                phone = request.phone.normalizeBlank(),
            )
        )

        return updatedMember.toMyProfileResponse()
    }
}
