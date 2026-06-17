package com.mymentalcare.server.application.auth.usecase

import com.mymentalcare.server.application.auth.*
import com.mymentalcare.server.application.auth.policy.*
import com.mymentalcare.server.application.auth.port.*
import com.mymentalcare.server.application.auth.reader.*
import com.mymentalcare.server.application.auth.request.*
import com.mymentalcare.server.application.auth.response.*

import com.mymentalcare.server.application.common.extension.logWarn
import com.mymentalcare.server.application.auth.port.JwtTokenIssuer
import com.mymentalcare.server.application.member.port.MemberRepository
import com.mymentalcare.server.application.auth.port.RefreshTokenStore
import org.springframework.stereotype.Service

@Service
internal class AuthenticationService(
    private val loginMemberReader: LoginMemberReader,
    private val passwordVerifier: PasswordVerifier,
    private val jwtTokenIssuer: JwtTokenIssuer,
    private val refreshTokenStore: RefreshTokenStore,
    private val memberRepository: MemberRepository,
) : AuthenticationInputPort {

    // 로그인
    override fun signIn(request: SignInRequest): SignInResponse {
        val member = loginMemberReader.readMemberByLoginIdentifier(request.identifier)

        passwordVerifier.verifyPasswordMatches(request.password, member.password)

        return issueLoginTokens(member.id)
    }

    // 리프레시 토큰으로 액세스 토큰을 재발급한다.
    override fun reissue(request: ReissueTokenRequest): SignInResponse {
        val memberId = jwtTokenIssuer.readMemberIdFromRefreshToken(request.refreshToken)
            ?: throwReissueFailed("anonymous", request.refreshToken, "refreshTokenInvalid")

        memberRepository.findById(memberId)
            ?: throwReissueFailed("memberId:$memberId", request.refreshToken, "memberNotFound")

        val storedRefreshToken = refreshTokenStore.readRefreshToken(memberId)
        if (storedRefreshToken != request.refreshToken) {
            throwReissueFailed("memberId:$memberId", request.refreshToken, "refreshTokenMismatch")
        }

        return issueLoginTokens(memberId)
    }

    // 로그인 방식이 달라도 동일한 JWT/refresh token 발급 정책을 재사용한다.
    fun issueLoginTokens(memberId: Long): SignInResponse {
        val accessToken = jwtTokenIssuer.issueAccessToken(memberId)
        val refreshToken = jwtTokenIssuer.issueRefreshToken(memberId)

        refreshTokenStore.storeRefreshToken(memberId, refreshToken)

        return SignInResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    private fun throwReissueFailed(who: String, refreshToken: String, reason: String): Nothing {
        logWarn {
            "[토큰 재발급] 리프레시 토큰 검증 실패. " +
                "who=$who, " +
                "what=POST /api/auth/reissue, " +
                "requestData=refreshToken:${refreshToken.take(12)}..., " +
                "reason=$reason"
        }
        throw TokenReissueFailedException()
    }
}
