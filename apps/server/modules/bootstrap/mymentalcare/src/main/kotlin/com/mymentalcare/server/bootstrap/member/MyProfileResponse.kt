package com.mymentalcare.server.bootstrap.member

data class MyProfileResponse(
    val memberId: Long,
    val loginId: String,
    val email: String?,
    val name: String,
    val phone: String?,
)
