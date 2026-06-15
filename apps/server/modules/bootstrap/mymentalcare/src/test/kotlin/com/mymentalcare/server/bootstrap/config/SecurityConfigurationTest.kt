package com.mymentalcare.server.bootstrap.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
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
@Import(SecurityConfiguration::class, WebCorsConfiguration::class)
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

    @Test
    fun `signup endpoint is permitted without authentication`() {
        mockMvc.perform(post("/api/members/signup"))
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
    @WithMockUser
    fun `actual access denied remains forbidden`() {
        mockMvc.perform(get("/api/forbidden-resource"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `localhost web origin can send cors preflight request`() {
        mockMvc.perform(
            options("/api/members/signup")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", HttpMethod.POST.name())
                .header("Access-Control-Request-Headers", "content-type"),
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
    }

    private fun expiredAccessToken(): String {
        val now = Instant.now()
        val key = Keys.hmacShaKeyFor(TEST_JWT_SECRET.toByteArray(StandardCharsets.UTF_8))

        return Jwts.builder()
            .setSubject("1")
            .setIssuedAt(Date.from(now.minusSeconds(120)))
            .setExpiration(Date.from(now.minusSeconds(60)))
            .claim("token_type", "access")
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }
}

@RestController
class SecurityTestController {
    @PostMapping("/api/members/signup")
    fun signup() {
    }

    @GetMapping("/api/protected-resource")
    fun protectedResource() {
    }

    @GetMapping("/api/forbidden-resource")
    fun forbiddenResource() {
        throw AccessDeniedException("forbidden")
    }
}
