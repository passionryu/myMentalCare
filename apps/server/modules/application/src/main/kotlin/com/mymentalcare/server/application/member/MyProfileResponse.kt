package com.mymentalcare.server.application.member

data class MyProfileResponse(
    val memberId: Long,
    val loginId: String,
    val email: String?,
    val name: String,
    val phone: String?,
)
