package com.mymentalcare.server.application.auth

data class OAuthLoginResult(
    val memberId: Long,
    val redirectTo: String,
)
