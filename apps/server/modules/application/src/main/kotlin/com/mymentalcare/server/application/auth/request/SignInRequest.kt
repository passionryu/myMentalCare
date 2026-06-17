package com.mymentalcare.server.application.auth.request

data class SignInRequest(
    val identifier: String,
    val password: String,
)
