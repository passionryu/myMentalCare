package com.mymentalcare.server.application.auth

import com.mymentalcare.server.application.port.JwtTokenIssuer
import com.mymentalcare.server.application.port.MemberRepository
import com.mymentalcare.server.application.port.RefreshTokenStore
import com.mymentalcare.server.domain.member.Member
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class AuthenticationServiceTest {
    @Test
    fun `저장된 리프레시 토큰과 요청 토큰이 일치하면 새로운 토큰을 발급한다`() {
        val jwtTokenIssuer = FakeJwtTokenIssuer(memberIdByRefreshToken = mapOf("old-refresh-token" to 1L))
        val refreshTokenStore = FakeRefreshTokenStore(storedTokens = mutableMapOf(1L to "old-refresh-token"))
        val service = authenticationService(
            jwtTokenIssuer = jwtTokenIssuer,
            refreshTokenStore = refreshTokenStore,
            memberRepository = FakeMemberRepository(member = testMember()),
        )

        val response = service.reissue(ReissueTokenRequest(refreshToken = "old-refresh-token"))

        assertEquals("new-access-token", response.accessToken)
        assertEquals("new-refresh-token", response.refreshToken)
        assertEquals("new-refresh-token", refreshTokenStore.readRefreshToken(1L))
    }

    @Test
    fun `저장된 리프레시 토큰과 요청 토큰이 다르면 재발급을 거부한다`() {
        val service = authenticationService(
            jwtTokenIssuer = FakeJwtTokenIssuer(memberIdByRefreshToken = mapOf("request-refresh-token" to 1L)),
            refreshTokenStore = FakeRefreshTokenStore(storedTokens = mutableMapOf(1L to "stored-refresh-token")),
            memberRepository = FakeMemberRepository(member = testMember()),
        )

        assertThrows(TokenReissueFailedException::class.java) {
            service.reissue(ReissueTokenRequest(refreshToken = "request-refresh-token"))
        }
    }

    @Test
    fun `리프레시 토큰에서 회원을 식별할 수 없으면 재발급을 거부한다`() {
        val service = authenticationService(
            jwtTokenIssuer = FakeJwtTokenIssuer(memberIdByRefreshToken = emptyMap()),
            refreshTokenStore = FakeRefreshTokenStore(storedTokens = mutableMapOf()),
            memberRepository = FakeMemberRepository(member = testMember()),
        )

        assertThrows(TokenReissueFailedException::class.java) {
            service.reissue(ReissueTokenRequest(refreshToken = "invalid-refresh-token"))
        }
    }

    private fun authenticationService(
        jwtTokenIssuer: JwtTokenIssuer,
        refreshTokenStore: RefreshTokenStore,
        memberRepository: MemberRepository,
    ): AuthenticationService {
        return AuthenticationService(
            loginMemberReader = LoginMemberReader(memberRepository),
            passwordVerifier = PasswordVerifier(BCryptPasswordEncoder()),
            jwtTokenIssuer = jwtTokenIssuer,
            refreshTokenStore = refreshTokenStore,
            memberRepository = memberRepository,
        )
    }

    private fun testMember(): Member {
        return Member(
            id = 1L,
            loginId = "test1",
            email = "test1@example.com",
            password = "encoded-password",
            name = "테스트회원",
            phone = "01012345678",
        )
    }

    private class FakeJwtTokenIssuer(
        private val memberIdByRefreshToken: Map<String, Long>,
    ) : JwtTokenIssuer {
        override fun issueAccessToken(memberId: Long): String = "new-access-token"

        override fun issueRefreshToken(memberId: Long): String = "new-refresh-token"

        override fun readMemberIdFromRefreshToken(refreshToken: String): Long? {
            return memberIdByRefreshToken[refreshToken]
        }
    }

    private class FakeRefreshTokenStore(
        private val storedTokens: MutableMap<Long, String>,
    ) : RefreshTokenStore {
        override fun storeRefreshToken(memberId: Long, refreshToken: String) {
            storedTokens[memberId] = refreshToken
        }

        override fun readRefreshToken(memberId: Long): String? {
            return storedTokens[memberId]
        }
    }

    private class FakeMemberRepository(
        private val member: Member?,
    ) : MemberRepository {
        override fun findByLoginIdOrEmail(identifier: String): Member? = member

        override fun findById(memberId: Long): Member? = member?.takeIf { it.id == memberId }

        override fun existsByLoginId(loginId: String): Boolean = false

        override fun save(member: Member): Member = member
    }
}
