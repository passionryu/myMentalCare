package com.mymentalcare.server.application.member

data class SignUpMemberRequest(
    val loginId: String,
    val email: String?,
    val password: String,
    val name: String,
    val phone: String?,
)
