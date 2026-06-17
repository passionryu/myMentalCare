package com.mymentalcare.server.application.member.request

data class UpdateMyProfileRequest(
    val name: String,
    val email: String?,
    val phone: String?,
)
