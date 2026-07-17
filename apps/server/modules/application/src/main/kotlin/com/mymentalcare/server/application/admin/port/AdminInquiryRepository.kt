package com.mymentalcare.server.application.admin.port

import com.mymentalcare.server.domain.inquiry.InquiryStatus
import java.time.LocalDateTime

interface AdminInquiryRepository {
    fun findInquiries(keyword: String?, status: InquiryStatus?, page: Int, size: Int): AdminInquiryPage

    fun findInquiryById(inquiryId: Long): AdminInquiryRecord?

    fun changeStatus(inquiryId: Long, status: InquiryStatus, adminMemo: String?, handledByMemberId: Long): AdminInquiryRecord?

    fun updateMemo(inquiryId: Long, adminMemo: String?): AdminInquiryRecord?
}

data class AdminInquiryPage(
    val inquiries: List<AdminInquiryRecord>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class AdminInquiryRecord(
    val id: Long,
    val memberId: Long,
    val category: String,
    val content: String,
    val status: InquiryStatus,
    val adminMemo: String?,
    val handledByMemberId: Long?,
    val handledAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
