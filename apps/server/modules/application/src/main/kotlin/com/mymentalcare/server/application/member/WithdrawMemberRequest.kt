package com.mymentalcare.server.application.member

data class WithdrawMemberRequest(
    val password: String,
    val confirmationText: String,
)

data class WithdrawMemberResponse(
    val withdrawn: Boolean,
)
