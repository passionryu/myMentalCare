package com.mymentalcare.server.application.inquiry.port

import com.mymentalcare.server.application.inquiry.request.*
import com.mymentalcare.server.application.inquiry.response.*

interface InquiryInputPort {
    fun createInquiry(memberId: Long, request: CreateInquiryRequest): CreateInquiryResponse
}
