package com.mymentalcare.server.infrastructure.persistence.inquiry

import com.mymentalcare.server.application.port.InquiryRepository
import com.mymentalcare.server.domain.inquiry.Inquiry
import org.springframework.stereotype.Repository

@Repository
class InquiryPersistenceAdapter(
    private val jpaInquiryRepository: JpaInquiryRepository,
) : InquiryRepository {
    override fun save(inquiry: Inquiry): Inquiry {
        return jpaInquiryRepository.save(inquiry.toEntity()).toDomain()
    }
}
