package com.mymentalcare.server.application.auth.response

data class OAuthLoginResult(
    val memberId: Long,
    val redirectTo: String,
)
