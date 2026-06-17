package com.mymentalcare.server.bootstrap.auth.web.response

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresInSeconds: Long,
)
