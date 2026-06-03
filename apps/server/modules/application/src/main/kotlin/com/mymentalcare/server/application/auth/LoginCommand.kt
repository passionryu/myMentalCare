package com.mymentalcare.server.application.auth

data class LoginCommand(
    val identifier: String,
    val password: String,
)
