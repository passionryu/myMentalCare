package com.mymentalcare.server.application.inquiry.port

import com.mymentalcare.server.application.inquiry.request.*
import com.mymentalcare.server.application.inquiry.response.*

import com.mymentalcare.server.domain.inquiry.Inquiry

interface InquiryRepository {
    fun save(inquiry: Inquiry): Inquiry
}
