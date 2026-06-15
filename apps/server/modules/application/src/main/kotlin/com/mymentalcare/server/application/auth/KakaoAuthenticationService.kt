package com.mymentalcare.server.application.auth

import com.mymentalcare.server.application.common.extension.logWarn
import com.mymentalcare.server.application.port.KakaoOAuthClient
import com.mymentalcare.server.application.port.MemberRepository
import com.mymentalcare.server.application.port.OAuthLoginResultStore
import com.mymentalcare.server.application.port.OAuthStateStore
import com.mymentalcare.server.application.port.SocialAccountRepository
import com.mymentalcare.server.domain.auth.OAuthProvider
import com.mymentalcare.server.domain.auth.SocialAccount
import com.mymentalcare.server.domain.member.Member
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.LocalDateTime
import java.util.Base64
import java.util.UUID

private val OAUTH_STATE_TTL: Duration = Duration.ofMinutes(5)
private val OAUTH_LOGIN_RESULT_TTL: Duration = Duration.ofMinutes(1)

@Service
internal class KakaoAuthenticationService(
    private val kakaoOAuthClient: KakaoOAuthClient,
    private val oAuthStateStore: OAuthStateStore,
    private val oAuthLoginResultStore: OAuthLoginResultStore,
    private val socialAccountRepository: SocialAccountRepository,
    private val memberRepository: MemberRepository,
    private val authenticationService: AuthenticationService,
    private val passwordEncoder: PasswordEncoder,
    private val redirectPolicy: OAuthRedirectPolicy,
) : KakaoAuthenticationInputPort {
    private val secureRandom = SecureRandom()

    @Transactional
    override fun startLogin(request: KakaoLoginStartRequest): KakaoLoginStartResponse {
        val state = generateOpaqueToken()
        val redirectTo = redirectPolicy.normalizeRedirectTo(request.redirectTo)

        oAuthStateStore.storeState(
            OAuthLoginState(
                state = state,
                redirectTo = redirectTo,
            ),
            OAUTH_STATE_TTL,
        )

        return KakaoLoginStartResponse(
            authorizationUrl = kakaoOAuthClient.buildAuthorizationUrl(state),
        )
    }

    @Transactional
    override fun handleCallback(request: KakaoCallbackRequest): KakaoCallbackResponse {
        val state = oAuthStateStore.consumeState(request.state)
            ?: throw OAuthStateInvalidException()

        val token = kakaoOAuthClient.fetchToken(request.code)
        val profile = kakaoOAuthClient.fetchProfile(token.accessToken)
        val member = readOrCreateKakaoMember(profile)

        val oneTimeCode = generateOpaqueToken()
        oAuthLoginResultStore.storeResult(
            oneTimeCode,
            OAuthLoginResult(
                memberId = member.id,
                redirectTo = state.redirectTo,
            ),
            OAUTH_LOGIN_RESULT_TTL,
        )

        return KakaoCallbackResponse(
            oneTimeCode = oneTimeCode,
            redirectTo = state.redirectTo,
        )
    }

    @Transactional
    override fun exchange(request: KakaoExchangeRequest): SignInResponse {
        val result = oAuthLoginResultStore.consumeResult(request.code)
            ?: throw OAuthExchangeCodeInvalidException()

        memberRepository.findById(result.memberId)
            ?: throw OAuthExchangeCodeInvalidException()

        return authenticationService.issueLoginTokens(result.memberId)
    }

    private fun readOrCreateKakaoMember(profile: KakaoProfile): Member {
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

    private fun generateOpaqueToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
