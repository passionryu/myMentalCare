package com.mymentalcare.server.application.auth

import com.mymentalcare.server.application.common.extension.logWarn
import com.mymentalcare.server.application.port.MemberRepository
import com.mymentalcare.server.domain.member.Member
import org.springframework.stereotype.Component

@Component
class LoginMemberReader(
    private val memberRepository: MemberRepository,
) {
    // 로그인 ID 또는 이메일 후보로 회원을 조회하고 없으면 안전한 실패 메시지로 차단한다.
    fun readMemberByLoginIdentifier(identifier: String): Member {
        return memberRepository.findByLoginIdOrEmail(identifier)
            ?: run {
                logWarn {
                    "[로그인] 로그인 대상 회원 조회 실패. " +
                        "who=anonymous, " +
                        "what=POST /api/auth/login, " +
                        "requestData=identifier:$identifier, " +
                        "reason=member_not_found"
                }
                throw LoginFailedException()
            }
    }
}
