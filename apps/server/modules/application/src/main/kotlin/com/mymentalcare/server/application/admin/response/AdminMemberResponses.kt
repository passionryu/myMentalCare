package com.mymentalcare.server.application.admin.response

import java.time.LocalDateTime

data class AdminMemberPageResponse(
    val members: List<AdminMemberSummaryResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class AdminMemberSummaryResponse(
    val id: Long,
    val loginId: String,
    val email: String?,
    val name: String,
    val status: String,
    val role: String,
    val createdAt: LocalDateTime,
)

data class AdminMemberDetailResponse(
    val id: Long,
    val loginId: String,
    val email: String?,
    val name: String,
    val phone: String?,
    val status: String,
    val role: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val deletedAt: LocalDateTime?,
)
