package com.mymentalcare.server.application.member

data class UpdateMyProfileRequest(
    val name: String,
    val email: String?,
    val phone: String?,
)
