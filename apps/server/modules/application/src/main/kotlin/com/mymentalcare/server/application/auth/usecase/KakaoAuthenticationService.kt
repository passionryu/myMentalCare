package com.mymentalcare.server.application.auth.usecase

import com.mymentalcare.server.application.auth.*
import com.mymentalcare.server.application.auth.policy.*
import com.mymentalcare.server.application.auth.port.*
import com.mymentalcare.server.application.auth.request.*
import com.mymentalcare.server.application.auth.response.*

import com.mymentalcare.server.application.auth.port.KakaoOAuthClient
import com.mymentalcare.server.application.member.port.MemberRepository
import com.mymentalcare.server.application.auth.port.OAuthLoginResultStore
import com.mymentalcare.server.application.auth.port.OAuthStateStore
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

private val OAUTH_STATE_TTL: Duration = Duration.ofMinutes(5)
private val OAUTH_LOGIN_RESULT_TTL: Duration = Duration.ofMinutes(1)

@Service
internal class KakaoAuthenticationService(
    private val kakaoOAuthClient: KakaoOAuthClient,
    private val oAuthStateStore: OAuthStateStore,
    private val oAuthLoginResultStore: OAuthLoginResultStore,
    private val memberRepository: MemberRepository,
    private val authenticationService: AuthenticationService,
    private val redirectPolicy: OAuthRedirectPolicy,
    private val kakaoMemberProvisioner: KakaoMemberProvisioner,
    private val oAuthOpaqueTokenGenerator: OAuthOpaqueTokenGenerator,
) : KakaoAuthenticationInputPort {
    @Transactional
    override fun startLogin(request: KakaoLoginStartRequest): KakaoLoginStartResponse {
        val state = oAuthOpaqueTokenGenerator.generateOpaqueToken()
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
        val member = kakaoMemberProvisioner.readOrCreateKakaoMember(profile)

        val oneTimeCode = oAuthOpaqueTokenGenerator.generateOpaqueToken()
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
}
