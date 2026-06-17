package com.mymentalcare.server.application.inquiry.usecase

import com.mymentalcare.server.application.inquiry.port.*
import com.mymentalcare.server.application.inquiry.request.*
import com.mymentalcare.server.application.inquiry.response.*

import com.mymentalcare.server.application.inquiry.port.InquiryRepository
import com.mymentalcare.server.domain.inquiry.Inquiry
import com.mymentalcare.server.domain.inquiry.InquiryStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
internal class InquiryService(
    private val inquiryRepository: InquiryRepository,
) : InquiryInputPort {
    @Transactional
    override fun createInquiry(memberId: Long, request: CreateInquiryRequest): CreateInquiryResponse {
        val now = LocalDateTime.now()
        val savedInquiry = inquiryRepository.save(
            Inquiry(
                id = 0,
                memberId = memberId,
                category = request.category.trim(),
                content = request.content.trim(),
                status = InquiryStatus.RECEIVED,
                createdAt = now,
            )
        )

        return CreateInquiryResponse(
            inquiryId = savedInquiry.id,
            createdAt = savedInquiry.createdAt ?: now,
            status = savedInquiry.status.name,
        )
    }
}
