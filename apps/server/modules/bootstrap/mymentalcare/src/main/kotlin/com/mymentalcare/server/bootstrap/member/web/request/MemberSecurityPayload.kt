package com.mymentalcare.server.bootstrap.member.web.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MemberLoginMethodsResponse(
    val passwordLoginEnabled: Boolean,
    val canChangePassword: Boolean,
    val socialAccounts: List<MemberSocialAccountResponse>,
)

data class MemberSocialAccountResponse(
    val provider: String,
    val email: String?,
    val linkedAt: String,
)

data class MemberPasswordChangePayload(
    @field:NotBlank(message = "현재 비밀번호를 입력해주세요.")
    val currentPassword: String,

    @field:NotBlank(message = "새 비밀번호를 입력해주세요.")
    @field:Size(min = 8, message = "새 비밀번호는 8자 이상 입력해주세요.")
    val newPassword: String,
)

data class MemberPasswordChangeResponse(
    val changed: Boolean,
)
