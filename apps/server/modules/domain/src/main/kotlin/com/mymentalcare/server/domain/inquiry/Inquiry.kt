package com.mymentalcare.server.domain.inquiry

import java.time.LocalDateTime

data class Inquiry(
    val id: Long,
    val memberId: Long,
    val category: String,
    val content: String,
    val status: InquiryStatus,
    val adminMemo: String? = null,
    val handledByMemberId: Long? = null,
    val handledAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
