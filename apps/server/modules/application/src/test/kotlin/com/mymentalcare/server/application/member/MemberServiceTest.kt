package com.mymentalcare.server.application.member

import com.mymentalcare.server.application.member.port.*
import com.mymentalcare.server.application.member.request.*
import com.mymentalcare.server.application.member.response.*
import com.mymentalcare.server.application.member.usecase.*

import com.mymentalcare.server.application.member.port.MemberNotificationSettingRepository
import com.mymentalcare.server.application.member.port.MemberRepository
import com.mymentalcare.server.application.auth.port.RefreshTokenStore
import com.mymentalcare.server.application.member.port.SocialAccountRepository
import com.mymentalcare.server.domain.auth.OAuthProvider
import com.mymentalcare.server.domain.auth.SocialAccount
import com.mymentalcare.server.domain.member.Member
import com.mymentalcare.server.domain.member.MemberNotificationSetting
import com.mymentalcare.server.domain.member.MemberStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.LocalDateTime
import java.time.LocalTime

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

    @Test
    fun `일반 계정은 로그인 방식 조회에서 비밀번호 변경 가능 상태를 반환한다`() {
        val service = memberService(
            repository = FakeMemberRepository(mutableMapOf(1L to testMember())),
        )

        val response = service.readLoginMethods(1L)

        assertEquals(true, response.passwordLoginEnabled)
        assertEquals(true, response.canChangePassword)
        assertEquals(0, response.socialAccounts.size)
    }

    @Test
    fun `카카오 계정은 소셜 연결 상태를 반환하고 비밀번호 변경을 막는다`() {
        val socialAccountRepository = FakeSocialAccountRepository(
            accounts = listOf(
                SocialAccount(
                    id = 1L,
                    memberId = 1L,
                    provider = OAuthProvider.KAKAO,
                    providerUserId = "kakao-1",
                    email = "kakao@example.com",
                    linkedAt = LocalDateTime.parse("2026-06-17T10:00:00"),
                )
            )
        )
        val service = memberService(
            repository = FakeMemberRepository(mutableMapOf(1L to testMember(loginId = "kakao_1234567890123"))),
            socialAccountRepository = socialAccountRepository,
        )

        val response = service.readLoginMethods(1L)

        assertEquals(false, response.passwordLoginEnabled)
        assertEquals(false, response.canChangePassword)
        assertEquals("KAKAO", response.socialAccounts.single().provider)
    }

    @Test
    fun `현재 비밀번호가 맞으면 새 비밀번호로 변경하고 리프레시 토큰을 제거한다`() {
        val repository = FakeMemberRepository(
            members = mutableMapOf(1L to testMember(password = passwordEncoder.encode("old-password"))),
        )
        val refreshTokenStore = FakeRefreshTokenStore(mutableMapOf(1L to "refresh-token"))
        val service = memberService(repository = repository, refreshTokenStore = refreshTokenStore)

        val response = service.changePassword(
            memberId = 1L,
            request = ChangeMemberPasswordRequest(currentPassword = "old-password", newPassword = "new-password"),
        )

        assertEquals(true, response.changed)
        assertEquals(true, passwordEncoder.matches("new-password", repository.members[1L]!!.password))
        assertNull(refreshTokenStore.readRefreshToken(1L))
    }

    @Test
    fun `알림 설정이 없으면 기본 알림 설정을 반환한다`() {
        val notificationSettingRepository = FakeMemberNotificationSettingRepository()
        val service = memberService(
            repository = FakeMemberRepository(mutableMapOf(1L to testMember())),
            notificationSettingRepository = notificationSettingRepository,
        )

        val response = service.readNotificationSetting(1L)

        assertEquals(false, response.enabled)
        assertEquals("21:00", response.notificationTime)
        assertEquals(listOf("MON", "TUE", "WED", "THU", "FRI"), response.weekdays)
        assertNull(notificationSettingRepository.findByMemberId(1L))
    }

    @Test
    fun `알림 설정 수정은 기존 설정을 갱신하고 요일 중복을 제거한다`() {
        val notificationSettingRepository = FakeMemberNotificationSettingRepository(
            settings = mutableMapOf(
                1L to MemberNotificationSetting(
                    id = 7L,
                    memberId = 1L,
                    enabled = false,
                    notificationTime = LocalTime.of(21, 0),
                    weekdays = listOf("MON"),
                ),
            ),
        )
        val service = memberService(
            repository = FakeMemberRepository(mutableMapOf(1L to testMember())),
            notificationSettingRepository = notificationSettingRepository,
        )

        val response = service.updateNotificationSetting(
            memberId = 1L,
            request = MemberNotificationSettingRequest(
                enabled = true,
                notificationTime = LocalTime.of(8, 30),
                weekdays = listOf("MON", "MON", "FRI"),
            ),
        )
        val savedSetting = notificationSettingRepository.findByMemberId(1L)

        assertEquals(true, response.enabled)
        assertEquals("08:30", response.notificationTime)
        assertEquals(listOf("MON", "FRI"), response.weekdays)
        assertEquals(7L, savedSetting?.id)
        assertEquals(LocalTime.of(8, 30), savedSetting?.notificationTime)
        assertEquals(listOf("MON", "FRI"), savedSetting?.weekdays)
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
        socialAccountRepository: SocialAccountRepository = FakeSocialAccountRepository(),
    ): MemberService {
        return MemberService(
            memberRepository = repository,
            notificationSettingRepository = notificationSettingRepository,
            refreshTokenStore = refreshTokenStore,
            socialAccountRepository = socialAccountRepository,
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

    private class FakeMemberNotificationSettingRepository(
        val settings: MutableMap<Long, MemberNotificationSetting> = mutableMapOf(),
    ) : MemberNotificationSettingRepository {
        override fun findByMemberId(memberId: Long): MemberNotificationSetting? = settings[memberId]

        override fun save(setting: MemberNotificationSetting): MemberNotificationSetting {
            val savedSetting = setting.copy(
                id = setting.id.takeIf { it > 0 }
                    ?: ((settings.values.maxOfOrNull { it.id } ?: 0L) + 1L),
            )
            settings[savedSetting.memberId] = savedSetting
            return savedSetting
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

        override fun deleteRefreshToken(memberId: Long) {
            storedTokens.remove(memberId)
        }
    }

    private class FakeSocialAccountRepository(
        private val accounts: List<SocialAccount> = emptyList(),
    ) : SocialAccountRepository {
        override fun findByProviderAndProviderUserId(provider: OAuthProvider, providerUserId: String): SocialAccount? {
            return accounts.firstOrNull { it.provider == provider && it.providerUserId == providerUserId }
        }

        override fun findByMemberId(memberId: Long): List<SocialAccount> {
            return accounts.filter { it.memberId == memberId }
        }

        override fun save(socialAccount: SocialAccount): SocialAccount {
            return socialAccount
        }
    }
}
