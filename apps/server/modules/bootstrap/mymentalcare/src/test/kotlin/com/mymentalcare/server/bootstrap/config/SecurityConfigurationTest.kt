package com.mymentalcare.server.bootstrap.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
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

@WebMvcTest(controllers = [SecurityTestController::class])
@Import(SecurityConfiguration::class, WebCorsConfiguration::class)
@EnableConfigurationProperties(JwtProperties::class)
@TestPropertySource(
    properties = [
        "mymentalcare.security.jwt.secret=test-secret",
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
}

@RestController
class SecurityTestController {
    @PostMapping("/api/members/signup")
    fun signup() {
    }

    @GetMapping("/api/protected-resource")
    fun protectedResource() {
    }
}
