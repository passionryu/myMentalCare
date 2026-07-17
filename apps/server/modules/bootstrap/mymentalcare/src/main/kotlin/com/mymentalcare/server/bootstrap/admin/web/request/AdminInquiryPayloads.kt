package com.mymentalcare.server.bootstrap.admin.web.request

import com.mymentalcare.server.domain.inquiry.InquiryStatus

data class AdminInquiryStatusChangePayload(
    val status: InquiryStatus,
    val adminMemo: String?,
)

data class AdminInquiryMemoPayload(
    val adminMemo: String?,
)
