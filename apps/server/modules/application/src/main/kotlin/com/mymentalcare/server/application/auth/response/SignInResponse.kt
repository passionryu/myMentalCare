package com.mymentalcare.server.application.auth.response

data class SignInResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long = 3600,
)
