package com.mymentalcare.server.application.port

import com.mymentalcare.server.domain.inquiry.Inquiry

interface InquiryRepository {
    fun save(inquiry: Inquiry): Inquiry
}
