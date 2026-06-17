package com.mymentalcare.server.application.inquiry.request

data class CreateInquiryRequest(
    val category: String,
    val content: String,
)
