package com.mymentalcare.server.bootstrap.member

data class MemberSignUpResponse(
    val memberId: Long,
    val loginId: String,
    val name: String,
)
