package com.mymentalcare.server.application.auth

import org.springframework.stereotype.Component

@Component
class OAuthRedirectPolicy {
    // 외부 URL로 인증 결과가 전달되지 않도록 내부 path 형태만 허용한다.
    fun normalizeRedirectTo(redirectTo: String?): String {
        val candidate = redirectTo?.trim().orEmpty()
        if (candidate.isBlank()) {
            return "/"
        }
        if (!candidate.startsWith("/") || candidate.startsWith("//")) {
            return "/"
        }
        if (candidate.contains("\n") || candidate.contains("\r")) {
            return "/"
        }
        return candidate
    }
}
