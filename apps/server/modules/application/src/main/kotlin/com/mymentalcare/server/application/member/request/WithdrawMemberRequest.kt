package com.mymentalcare.server.application.member.request

data class WithdrawMemberRequest(
    val password: String,
    val confirmationText: String,
)

data class WithdrawMemberResponse(
    val withdrawn: Boolean,
)
