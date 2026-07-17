package com.mymentalcare.server.bootstrap.admin.web.response

import java.time.LocalDateTime

data class AdminInquiryPageResponse(
    val inquiries: List<AdminInquirySummaryResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class AdminInquirySummaryResponse(
    val id: Long,
    val memberId: Long,
    val category: String,
    val status: String,
    val createdAt: LocalDateTime,
)

data class AdminInquiryDetailResponse(
    val id: Long,
    val memberId: Long,
    val category: String,
    val content: String,
    val status: String,
    val adminMemo: String?,
    val handledByMemberId: Long?,
    val handledAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
