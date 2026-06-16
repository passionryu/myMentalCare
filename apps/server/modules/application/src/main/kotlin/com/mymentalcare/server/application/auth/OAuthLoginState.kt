package com.mymentalcare.server.application.auth

data class OAuthLoginState(
    val state: String,
    val redirectTo: String,
)
