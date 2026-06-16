package com.mymentalcare.server.domain.inquiry

import java.time.LocalDateTime

data class Inquiry(
    val id: Long,
    val memberId: Long,
    val category: String,
    val content: String,
    val status: InquiryStatus,
    val createdAt: LocalDateTime? = null,
)
