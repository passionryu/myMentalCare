package com.mymentalcare.server.bootstrap.inquiry

import java.time.LocalDateTime

data class CreateInquiryResponse(
    val inquiryId: Long,
    val createdAt: LocalDateTime,
    val status: String,
)
