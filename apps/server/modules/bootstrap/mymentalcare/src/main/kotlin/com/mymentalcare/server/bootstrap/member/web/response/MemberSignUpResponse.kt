package com.mymentalcare.server.bootstrap.member.web.response

data class MemberSignUpResponse(
    val memberId: Long,
    val loginId: String,
    val name: String,
)
