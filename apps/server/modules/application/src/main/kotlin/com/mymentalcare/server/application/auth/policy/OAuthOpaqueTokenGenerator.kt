package com.mymentalcare.server.application.auth.policy

import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64

@Component
internal class OAuthOpaqueTokenGenerator {
    private val secureRandom = SecureRandom()

    // OAuth state와 one-time code에 사용할 추측 불가능한 토큰을 생성한다.
    fun generateOpaqueToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
