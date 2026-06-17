package com.mymentalcare.server.application.member.response

data class SignUpMemberResponse(
    val memberId: Long,
    val loginId: String,
    val name: String,
)
