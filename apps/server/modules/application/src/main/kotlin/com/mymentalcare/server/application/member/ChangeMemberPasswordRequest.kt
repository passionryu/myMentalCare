package com.mymentalcare.server.application.member

data class ChangeMemberPasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)

data class ChangeMemberPasswordResponse(
    val changed: Boolean,
)
