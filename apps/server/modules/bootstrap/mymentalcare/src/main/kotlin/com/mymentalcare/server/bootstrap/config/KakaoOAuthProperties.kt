package com.mymentalcare.server.bootstrap.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "mymentalcare.auth.kakao")
data class KakaoOAuthProperties(
    val clientId: String = "",
    val clientSecret: String = "",
    val redirectUri: String = "http://localhost:3001/api/auth/kakao/callback",
    val webCallbackUrl: String = "http://localhost:3000/auth/kakao/callback",
    val authorizeUrl: String = "https://kauth.kakao.com/oauth/authorize",
    val tokenUrl: String = "https://kauth.kakao.com/oauth/token",
    val profileUrl: String = "https://kapi.kakao.com/v2/user/me",
    val timeout: Duration = Duration.ofSeconds(5),
)
