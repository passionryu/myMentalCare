package com.mymentalcare.server.application.auth.response

data class KakaoProfile(
    val providerUserId: String,
    val email: String?,
    val nickname: String?,
)
