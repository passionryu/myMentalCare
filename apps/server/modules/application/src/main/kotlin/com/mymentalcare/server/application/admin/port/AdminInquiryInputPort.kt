package com.mymentalcare.server.application.admin.port

import com.mymentalcare.server.application.admin.request.AdminInquiryMemoRequest
import com.mymentalcare.server.application.admin.request.AdminInquirySearchRequest
import com.mymentalcare.server.application.admin.request.AdminInquiryStatusChangeRequest
import com.mymentalcare.server.application.admin.response.AdminInquiryDetailResponse
import com.mymentalcare.server.application.admin.response.AdminInquiryPageResponse

interface AdminInquiryQueryInputPort {
    fun readInquiries(request: AdminInquirySearchRequest): AdminInquiryPageResponse

    fun readInquiry(adminMemberId: Long, inquiryId: Long): AdminInquiryDetailResponse
}

interface AdminInquiryCommandInputPort {
    fun changeInquiryStatus(request: AdminInquiryStatusChangeRequest): AdminInquiryDetailResponse

    fun updateInquiryMemo(request: AdminInquiryMemoRequest): AdminInquiryDetailResponse
}
