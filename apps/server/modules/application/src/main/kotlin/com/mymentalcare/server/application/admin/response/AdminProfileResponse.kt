package com.mymentalcare.server.application.admin.response

data class AdminProfileResponse(
    val memberId: Long,
    val loginId: String,
    val name: String,
    val role: String,
)
