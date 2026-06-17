package com.mymentalcare.server.application.member.response

data class MyProfileResponse(
    val memberId: Long,
    val loginId: String,
    val email: String?,
    val name: String,
    val phone: String?,
)
