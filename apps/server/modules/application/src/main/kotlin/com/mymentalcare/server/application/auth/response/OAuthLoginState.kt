package com.mymentalcare.server.application.auth.response

data class OAuthLoginState(
    val state: String,
    val redirectTo: String,
)
