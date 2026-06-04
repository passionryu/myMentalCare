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
}
