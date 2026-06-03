package com.mymentalcare.server.application.auth

data class SignInRequest(
    val identifier: String,
    val password: String,
)
