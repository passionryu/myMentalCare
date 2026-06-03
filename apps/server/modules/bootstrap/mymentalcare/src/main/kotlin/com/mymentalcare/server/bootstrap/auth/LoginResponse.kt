package com.mymentalcare.server.bootstrap.auth

import com.mymentalcare.server.application.auth.LoginResult

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresInSeconds: Long,
)
