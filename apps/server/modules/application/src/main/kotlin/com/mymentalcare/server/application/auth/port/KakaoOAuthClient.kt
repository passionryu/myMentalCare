package com.mymentalcare.server.application.auth.port

import com.mymentalcare.server.application.auth.request.*
import com.mymentalcare.server.application.auth.response.*

import com.mymentalcare.server.application.auth.response.KakaoProfile
import com.mymentalcare.server.application.auth.response.KakaoTokenResponse

interface KakaoOAuthClient {
    fun buildAuthorizationUrl(state: String): String

    fun fetchToken(code: String): KakaoTokenResponse

    fun fetchProfile(accessToken: String): KakaoProfile
}
