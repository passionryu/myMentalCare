package com.mymentalcare.server.application.auth

import com.mymentalcare.server.application.port.JwtTokenIssuer
import com.mymentalcare.server.application.port.RefreshTokenStore
import org.springframework.stereotype.Service

@Service
class LoginUseCase(
    private val loginMemberReader: LoginMemberReader,
    private val passwordVerifier: PasswordVerifier,
    private val jwtTokenIssuer: JwtTokenIssuer,
    private val refreshTokenStore: RefreshTokenStore,
) {
    fun loginMember(command: LoginCommand): LoginResult {
        val member = loginMemberReader.readMemberByLoginIdentifier(command.identifier)

        passwordVerifier.verifyPasswordMatches(command.password, member.password)

        val accessToken = jwtTokenIssuer.issueAccessToken(member.id)
        val refreshToken = jwtTokenIssuer.issueRefreshToken(member.id)

        refreshTokenStore.storeRefreshToken(member.id, refreshToken)

        return LoginResult(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}
