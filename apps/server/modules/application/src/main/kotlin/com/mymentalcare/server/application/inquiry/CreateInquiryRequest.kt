package com.mymentalcare.server.application.inquiry

data class CreateInquiryRequest(
    val category: String,
    val content: String,
)
