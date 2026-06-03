package com.mymentalcare.server.application.auth

import com.mymentalcare.server.application.port.JwtTokenIssuer
import com.mymentalcare.server.application.port.RefreshTokenStore
import org.springframework.stereotype.Service

@Service
internal class AuthenticationService(
    private val loginMemberReader: LoginMemberReader,
    private val passwordVerifier: PasswordVerifier,
    private val jwtTokenIssuer: JwtTokenIssuer,
    private val refreshTokenStore: RefreshTokenStore,
) : AuthenticationInputPort {
    override fun signIn(request: SignInRequest): SignInResponse {
        val member = loginMemberReader.readMemberByLoginIdentifier(request.identifier)

        passwordVerifier.verifyPasswordMatches(request.password, member.password)

        val accessToken = jwtTokenIssuer.issueAccessToken(member.id)
        val refreshToken = jwtTokenIssuer.issueRefreshToken(member.id)

        refreshTokenStore.storeRefreshToken(member.id, refreshToken)

        return SignInResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}
