package com.mymentalcare.server.application.inquiry

import java.time.LocalDateTime

data class CreateInquiryResponse(
    val inquiryId: Long,
    val createdAt: LocalDateTime,
    val status: String,
)
