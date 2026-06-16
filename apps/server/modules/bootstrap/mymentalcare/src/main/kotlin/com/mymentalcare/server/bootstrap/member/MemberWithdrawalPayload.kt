package com.mymentalcare.server.bootstrap.member

import jakarta.validation.constraints.NotBlank

data class MemberWithdrawalPayload(
    @field:NotBlank(message = "비밀번호를 입력해주세요.")
    val password: String,

    @field:NotBlank(message = "확인 문구를 입력해주세요.")
    val confirmationText: String,
)

data class MemberWithdrawalResponse(
    val withdrawn: Boolean,
)
