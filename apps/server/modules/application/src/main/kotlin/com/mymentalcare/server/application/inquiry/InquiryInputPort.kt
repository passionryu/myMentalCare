package com.mymentalcare.server.application.inquiry

interface InquiryInputPort {
    fun createInquiry(memberId: Long, request: CreateInquiryRequest): CreateInquiryResponse
}
