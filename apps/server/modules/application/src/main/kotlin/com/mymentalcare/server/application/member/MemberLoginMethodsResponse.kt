package com.mymentalcare.server.application.member

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
