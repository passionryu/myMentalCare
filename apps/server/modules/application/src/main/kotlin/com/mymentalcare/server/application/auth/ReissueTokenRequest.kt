package com.mymentalcare.server.application.auth

data class ReissueTokenRequest(
    val refreshToken: String,
)
