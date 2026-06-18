package com.mymentalcare.server.application.auth.usecase

import com.mymentalcare.server.application.auth.KakaoAccountConflictException
import com.mymentalcare.server.application.auth.KakaoAuthFailedException
import com.mymentalcare.server.application.auth.response.KakaoProfile
import com.mymentalcare.server.application.common.extension.logWarn
import com.mymentalcare.server.application.member.port.MemberRepository
import com.mymentalcare.server.application.member.port.SocialAccountRepository
import com.mymentalcare.server.domain.auth.OAuthProvider
import com.mymentalcare.server.domain.auth.SocialAccount
import com.mymentalcare.server.domain.member.Member
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.UUID

@Component
internal class KakaoMemberProvisioner(
    private val socialAccountRepository: SocialAccountRepository,
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    private val secureRandom = SecureRandom()

    // 카카오 프로필에 대응하는 회원을 조회하거나 새 소셜 회원을 생성한다.
    fun readOrCreateKakaoMember(profile: KakaoProfile): Member {
        val existingSocialAccount = socialAccountRepository.findByProviderAndProviderUserId(
            OAuthProvider.KAKAO,
            profile.providerUserId,
        )
        if (existingSocialAccount != null) {
            return memberRepository.findById(existingSocialAccount.memberId)
                ?: throw KakaoAuthFailedException()
        }

        val email = profile.email?.takeIf { it.isNotBlank() }
        if (email != null && memberRepository.findByEmail(email) != null) {
            logWarn {
                "[카카오 로그인] 기존 이메일 계정과 충돌하여 자동 연결 차단. " +
                    "who=anonymous, " +
                    "what=GET /api/auth/kakao/callback, " +
                    "requestData=email:$email, " +
                    "reason=email_conflict"
            }
            throw KakaoAccountConflictException()
        }

        val member = memberRepository.save(
            Member(
                id = 0,
                loginId = generateKakaoLoginId(profile.providerUserId),
                email = email,
                password = passwordEncoder.encode("social:${UUID.randomUUID()}"),
                name = profile.nickname?.takeIf { it.isNotBlank() } ?: "카카오 사용자",
                phone = null,
            )
        )

        socialAccountRepository.save(
            SocialAccount(
                id = 0,
                memberId = member.id,
                provider = OAuthProvider.KAKAO,
                providerUserId = profile.providerUserId,
                email = email,
                linkedAt = LocalDateTime.now(),
            )
        )

        return member
    }

    private fun generateKakaoLoginId(providerUserId: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(providerUserId.toByteArray())
            .joinToString("") { "%02x".format(it) }
        val base = "kakao_${digest.take(13)}"
        if (!memberRepository.existsByLoginId(base)) {
            return base
        }

        return "kakao_${digest.take(10)}${secureRandom.nextInt(1000).toString().padStart(3, '0')}"
    }
}
