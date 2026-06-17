package com.mymentalcare.server.application.auth.request

data class ReissueTokenRequest(
    val refreshToken: String,
)
