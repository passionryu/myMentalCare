package com.mymentalcare.server.application.auth

import com.mymentalcare.server.application.auth.policy.*
import com.mymentalcare.server.application.auth.port.*
import com.mymentalcare.server.application.auth.reader.*
import com.mymentalcare.server.application.auth.request.*
import com.mymentalcare.server.application.auth.response.*
import com.mymentalcare.server.application.auth.usecase.*

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OAuthRedirectPolicyTest {
    private val policy = OAuthRedirectPolicy()

    @Test
    fun `내부 경로는 로그인 후 이동 경로로 유지한다`() {
        assertEquals("/chat?from=kakao", policy.normalizeRedirectTo("/chat?from=kakao"))
    }

    @Test
    fun `외부 URL과 프로토콜 상대 URL은 기본 경로로 대체한다`() {
        assertEquals("/", policy.normalizeRedirectTo("https://evil.example/path"))
        assertEquals("/", policy.normalizeRedirectTo("//evil.example/path"))
    }

    @Test
    fun `빈 값과 개행 포함 경로는 기본 경로로 대체한다`() {
        assertEquals("/", policy.normalizeRedirectTo(null))
        assertEquals("/", policy.normalizeRedirectTo(""))
        assertEquals("/", policy.normalizeRedirectTo("/chat\nSet-Cookie:bad=true"))
    }
}
