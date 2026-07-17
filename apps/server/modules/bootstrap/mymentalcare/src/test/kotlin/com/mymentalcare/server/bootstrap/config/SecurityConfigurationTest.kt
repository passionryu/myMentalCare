package com.mymentalcare.server.bootstrap.config

import com.mymentalcare.server.application.member.port.MemberRepository
import com.mymentalcare.server.domain.member.Member
import com.mymentalcare.server.domain.member.MemberRole
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date

private const val TEST_JWT_SECRET = "test-secret-key-for-jwt-claims-1234"

@WebMvcTest(controllers = [SecurityTestController::class])
@Import(SecurityConfiguration::class, WebCorsConfiguration::class, SecurityTestRepositoryConfiguration::class)
@EnableConfigurationProperties(JwtProperties::class)
@TestPropertySource(
    properties = [
        "mymentalcare.security.jwt.secret=$TEST_JWT_SECRET",
        "mymentalcare.security.jwt.access-token-expiration=1h",
        "mymentalcare.security.jwt.refresh-token-expiration=7d",
    ],
)
class SecurityConfigurationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        SecurityTestMemberRepository.members.clear()
    }

    @Test
    fun `signup endpoint is permitted without authentication`() {
        mockMvc.perform(post("/api/members/signup"))
            .andExpect(status().isOk)
    }

    @Test
    fun `kakao login endpoints are permitted without authentication`() {
        mockMvc.perform(get("/api/auth/kakao/login"))
            .andExpect(status().isOk)

        mockMvc.perform(get("/api/auth/kakao/callback"))
            .andExpect(status().isOk)

        mockMvc.perform(post("/api/auth/kakao/exchange"))
            .andExpect(status().isOk)
    }

    @Test
    fun `protected api requires authentication`() {
        mockMvc.perform(get("/api/protected-resource"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `expired access token is treated as authentication failure`() {
        mockMvc.perform(
            get("/api/protected-resource")
                .header("Authorization", "Bearer ${expiredAccessToken()}"),
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `admin api requires admin role`() {
        SecurityTestMemberRepository.members[1L] = testMember(role = MemberRole.USER)

        mockMvc.perform(
            get("/api/admin/protected-resource")
                .header("Authorization", "Bearer ${accessToken(memberId = 1L)}"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `admin api permits admin role`() {
        SecurityTestMemberRepository.members[2L] = testMember(id = 2L, role = MemberRole.ADMIN)

        mockMvc.perform(
            get("/api/admin/protected-resource")
                .header("Authorization", "Bearer ${accessToken(memberId = 2L)}"),
        )
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser
    fun `actual access denied remains forbidden`() {
        mockMvc.perform(get("/api/forbidden-resource"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `local development web origins can send cors preflight request`() {
        listOf(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://10.0.2.2:3000",
            "http://192.168.1.112:3000",
        ).forEach { origin ->
            mockMvc.perform(
                options("/api/members/signup")
                    .header("Origin", origin)
                    .header("Access-Control-Request-Method", HttpMethod.POST.name())
                    .header("Access-Control-Request-Headers", "content-type"),
            )
                .andExpect(status().isOk)
                .andExpect(header().string("Access-Control-Allow-Origin", origin))
        }
    }

    private fun expiredAccessToken(): String {
        return accessToken(memberId = 1L, expirationOffsetSeconds = -60, issuedAtOffsetSeconds = -120)
    }

    private fun accessToken(
        memberId: Long,
        expirationOffsetSeconds: Long = 3600,
        issuedAtOffsetSeconds: Long = 0,
    ): String {
        val now = Instant.now()
        val key = Keys.hmacShaKeyFor(TEST_JWT_SECRET.toByteArray(StandardCharsets.UTF_8))

        return Jwts.builder()
            .setSubject(memberId.toString())
            .setIssuedAt(Date.from(now.plusSeconds(issuedAtOffsetSeconds)))
            .setExpiration(Date.from(now.plusSeconds(expirationOffsetSeconds)))
            .claim("token_type", "access")
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    private fun testMember(
        id: Long = 1L,
        role: MemberRole = MemberRole.USER,
    ): Member {
        return Member(
            id = id,
            loginId = "member$id",
            email = "member$id@example.com",
            password = "encoded",
            name = "테스트회원",
            phone = null,
            role = role,
        )
    }
}

@TestConfiguration
class SecurityTestRepositoryConfiguration {
    @Bean
    fun memberRepository(): MemberRepository = SecurityTestMemberRepository
}

object SecurityTestMemberRepository : MemberRepository {
    val members: MutableMap<Long, Member> = mutableMapOf()

    override fun findByLoginIdOrEmail(identifier: String): Member? = null

    override fun findById(memberId: Long): Member? = members[memberId]

    override fun findByEmail(email: String): Member? = null

    override fun existsByLoginId(loginId: String): Boolean = false

    override fun save(member: Member): Member = member

    override fun withdraw(member: Member): Member = member
}

@RestController
class SecurityTestController {
    @PostMapping("/api/members/signup")
    fun signup() {
    }

    @GetMapping("/api/auth/kakao/login")
    fun kakaoLogin() {
    }

    @GetMapping("/api/auth/kakao/callback")
    fun kakaoCallback() {
    }

    @PostMapping("/api/auth/kakao/exchange")
    fun kakaoExchange() {
    }

    @GetMapping("/api/protected-resource")
    fun protectedResource() {
    }

    @GetMapping("/api/forbidden-resource")
    fun forbiddenResource() {
        throw AccessDeniedException("forbidden")
    }

    @GetMapping("/api/admin/protected-resource")
    fun adminProtectedResource() {
    }
}
