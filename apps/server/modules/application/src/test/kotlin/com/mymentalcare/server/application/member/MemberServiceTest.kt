package com.mymentalcare.server.application.member

import com.mymentalcare.server.application.port.MemberRepository
import com.mymentalcare.server.domain.member.Member
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class MemberServiceTest {
    @Test
    fun `내 프로필을 수정하면 이름 이메일 전화번호가 갱신된다`() {
        val repository = FakeMemberRepository(
            members = mutableMapOf(1L to testMember()),
        )
        val service = MemberService(repository, BCryptPasswordEncoder())

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
        val service = MemberService(repository, BCryptPasswordEncoder())

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
        val service = MemberService(repository, BCryptPasswordEncoder())

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

    private fun testMember(
        id: Long = 1L,
        loginId: String = "test1",
        email: String? = "test1@example.com",
    ): Member {
        return Member(
            id = id,
            loginId = loginId,
            email = email,
            password = "encoded-password",
            name = "테스트회원",
            phone = "01012345678",
        )
    }

    private class FakeMemberRepository(
        private val members: MutableMap<Long, Member>,
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
    }
}
