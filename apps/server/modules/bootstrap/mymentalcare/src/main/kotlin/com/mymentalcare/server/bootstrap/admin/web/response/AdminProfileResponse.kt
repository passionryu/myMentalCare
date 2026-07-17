package com.mymentalcare.server.bootstrap.admin.web.response

data class AdminProfileResponse(
    val memberId: Long,
    val loginId: String,
    val name: String,
    val role: String,
)
