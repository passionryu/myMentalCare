package com.mymentalcare.server.application.auth

data class LoginAuthCommand(
    val requestedBy: String? = null,
)
