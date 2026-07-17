package com.mymentalcare.server.application.admin.request

import com.mymentalcare.server.domain.inquiry.InquiryStatus

data class AdminInquirySearchRequest(
    val keyword: String? = null,
    val status: InquiryStatus? = null,
    val page: Int = 0,
    val size: Int = 20,
) {
    fun normalized(): AdminInquirySearchRequest {
        return copy(
            keyword = keyword?.trim()?.takeIf { it.isNotBlank() },
            page = page.coerceAtLeast(0),
            size = size.coerceIn(1, 50),
        )
    }
}

data class AdminInquiryStatusChangeRequest(
    val adminMemberId: Long,
    val inquiryId: Long,
    val status: InquiryStatus,
    val adminMemo: String?,
)

data class AdminInquiryMemoRequest(
    val adminMemberId: Long,
    val inquiryId: Long,
    val adminMemo: String?,
)
