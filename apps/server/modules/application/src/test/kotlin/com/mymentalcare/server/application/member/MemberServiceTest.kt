package com.mymentalcare.server.application.member

import com.mymentalcare.server.application.port.MemberNotificationSettingRepository
import com.mymentalcare.server.application.port.MemberRepository
import com.mymentalcare.server.application.port.RefreshTokenStore
import com.mymentalcare.server.domain.member.Member
import com.mymentalcare.server.domain.member.MemberNotificationSetting
import com.mymentalcare.server.domain.member.MemberStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class MemberServiceTest {
    private val passwordEncoder = BCryptPasswordEncoder()

    @Test
    fun `내 프로필을 수정하면 이름 이메일 전화번호가 갱신된다`() {
        val repository = FakeMemberRepository(
            members = mutableMapOf(1L to testMember()),
        )
        val service = memberService(repository)

        val response = service.updateMyProfile(
            memberId = 1L,
            request = UpdateMyProfileRequest(
                name = "수정회원",
                email = "updated@example.com",
                phone = "010-9999-8888",
            ),
        )

        assertEquals("수정회원", response.name)
        assertEquals("updated@example.com", response.email)
        assertEquals("010-9999-8888", response.phone)
        assertEquals("test1", repository.findById(1L)?.loginId)
        assertEquals("encoded-password", repository.findById(1L)?.password)
    }

    @Test
    fun `빈 이메일과 전화번호는 null로 저장한다`() {
        val repository = FakeMemberRepository(
            members = mutableMapOf(1L to testMember()),
        )
        val service = memberService(repository)

        val response = service.updateMyProfile(
            memberId = 1L,
            request = UpdateMyProfileRequest(
                name = "공백정리",
                email = "   ",
                phone = "",
            ),
        )

        assertNull(response.email)
        assertNull(response.phone)
    }

    @Test
    fun `다른 회원의 이메일로 수정하면 중복 이메일 예외를 던진다`() {
        val repository = FakeMemberRepository(
            members = mutableMapOf(
                1L to testMember(),
                2L to testMember(id = 2L, loginId = "test2", email = "used@example.com"),
            ),
        )
        val service = memberService(repository)

        assertThrows(DuplicateEmailException::class.java) {
            service.updateMyProfile(
                memberId = 1L,
                request = UpdateMyProfileRequest(
                    name = "수정회원",
                    email = "used@example.com",
                    phone = "010-9999-8888",
                ),
            )
        }
    }

    @Test
    fun `비밀번호와 확인 문구가 맞으면 회원을 탈퇴 처리하고 리프레시 토큰을 제거한다`() {
        val repository = FakeMemberRepository(
            members = mutableMapOf(1L to testMember(password = passwordEncoder.encode("password123"))),
        )
        val refreshTokenStore = FakeRefreshTokenStore(mutableMapOf(1L to "refresh-token"))
        val service = memberService(repository = repository, refreshTokenStore = refreshTokenStore)

        val response = service.withdrawMyAccount(
            memberId = 1L,
            request = WithdrawMemberRequest(password = "password123", confirmationText = "회원 탈퇴"),
        )

        assertEquals(true, response.withdrawn)
        assertEquals(MemberStatus.WITHDRAWN, repository.members[1L]?.status)
        assertNull(refreshTokenStore.readRefreshToken(1L))
    }

    @Test
    fun `비밀번호가 틀리면 회원 탈퇴를 거부한다`() {
        val repository = FakeMemberRepository(
            members = mutableMapOf(1L to testMember(password = passwordEncoder.encode("password123"))),
        )
        val service = memberService(repository)

        assertThrows(MemberWithdrawalFailedException::class.java) {
            service.withdrawMyAccount(
                memberId = 1L,
                request = WithdrawMemberRequest(password = "wrong-password", confirmationText = "회원 탈퇴"),
            )
        }
    }

    private fun testMember(
        id: Long = 1L,
        loginId: String = "test1",
        email: String? = "test1@example.com",
        password: String = "encoded-password",
    ): Member {
        return Member(
            id = id,
            loginId = loginId,
            email = email,
            password = password,
            name = "테스트회원",
            phone = "01012345678",
        )
    }

    private fun memberService(
        repository: MemberRepository,
        notificationSettingRepository: MemberNotificationSettingRepository = FakeMemberNotificationSettingRepository(),
        refreshTokenStore: RefreshTokenStore = FakeRefreshTokenStore(mutableMapOf()),
    ): MemberService {
        return MemberService(
            memberRepository = repository,
            notificationSettingRepository = notificationSettingRepository,
            refreshTokenStore = refreshTokenStore,
            passwordEncoder = BCryptPasswordEncoder(),
        )
    }

    private class FakeMemberRepository(
        val members: MutableMap<Long, Member>,
    ) : MemberRepository {
        override fun findByLoginIdOrEmail(identifier: String): Member? {
            return members.values.firstOrNull { it.loginId == identifier || it.email == identifier }
        }

        override fun findById(memberId: Long): Member? = members[memberId]

        override fun findByEmail(email: String): Member? = members.values.firstOrNull { it.email == email }

        override fun existsByLoginId(loginId: String): Boolean = members.values.any { it.loginId == loginId }

        override fun save(member: Member): Member {
            val savedMember = member.copy(id = member.id.takeIf { it > 0 } ?: ((members.keys.maxOrNull() ?: 0L) + 1L))
            members[savedMember.id] = savedMember
            return savedMember
        }

        override fun withdraw(member: Member): Member {
            val withdrawnMember = member.copy(status = MemberStatus.WITHDRAWN)
            members[withdrawnMember.id] = withdrawnMember
            return withdrawnMember
        }
    }

    private class FakeMemberNotificationSettingRepository : MemberNotificationSettingRepository {
        override fun findByMemberId(memberId: Long): MemberNotificationSetting? = null

        override fun save(setting: MemberNotificationSetting): MemberNotificationSetting = setting
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

        override fun deleteRefreshToken(memberId: Long) {
            storedTokens.remove(memberId)
        }
    }
}
