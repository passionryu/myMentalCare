package com.mymentalcare.server.application.port

import com.mymentalcare.server.application.auth.KakaoProfile
import com.mymentalcare.server.application.auth.KakaoTokenResponse

interface KakaoOAuthClient {
    fun buildAuthorizationUrl(state: String): String

    fun fetchToken(code: String): KakaoTokenResponse

    fun fetchProfile(accessToken: String): KakaoProfile
}
