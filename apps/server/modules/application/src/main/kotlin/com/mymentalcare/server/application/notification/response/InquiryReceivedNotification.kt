package com.mymentalcare.server.application.notification.response

import java.time.LocalDateTime

data class InquiryReceivedNotification(
    val inquiryId: Long,
    val memberId: Long,
    val category: String,
    val content: String,
    val receivedAt: LocalDateTime,
)
