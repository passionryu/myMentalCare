package com.mymentalcare.server.application.member

import com.mymentalcare.server.application.common.extension.logWarn
import com.mymentalcare.server.application.port.MemberRepository
import com.mymentalcare.server.domain.member.Member
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
) : MemberInputPort {
    // 회원가입
    @Transactional
    override fun signUp(request: SignUpMemberRequest): SignUpMemberResponse {
        if (memberRepository.existsByLoginId(request.loginId)) {
            logWarn {
                "[회원가입] 로그인 아이디 중복으로 회원가입 실패. " +
                    "who=anonymous, " +
                    "what=POST /api/members/signup, " +
                    "requestData=loginId:${request.loginId},email:${request.email ?: "none"}, " +
                    "reason=duplicateLoginId"
            }
            throw DuplicateLoginIdException()
        }

        val member = memberRepository.save(
            Member(
                id = 0,
                loginId = request.loginId,
                email = request.email?.takeIf { it.isNotBlank() },
                password = passwordEncoder.encode(request.password),
                name = request.name,
                phone = request.phone?.takeIf { it.isNotBlank() },
            )
        )

        return SignUpMemberResponse(
            memberId = member.id,
            loginId = member.loginId,
            name = member.name,
        )
    }

    // 내 프로필 조회
    @Transactional(readOnly = true)
    override fun readMyProfile(memberId: Long): MyProfileResponse {
        val member = memberRepository.findById(memberId)
            ?: throw MemberNotFoundException()

        return member.toMyProfileResponse()
    }

    // 내 프로필 수정
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

    private fun Member.toMyProfileResponse(): MyProfileResponse {
        return MyProfileResponse(
            memberId = id,
            loginId = loginId,
            email = email,
            name = name,
            phone = phone,
        )
    }

    private fun String?.normalizeBlank(): String? = this?.trim()?.takeIf { it.isNotBlank() }
}
