package com.mymentalcare.server.bootstrap.admin.web

import com.mymentalcare.server.application.admin.port.AdminInquiryCommandInputPort
import com.mymentalcare.server.application.admin.port.AdminInquiryQueryInputPort
import com.mymentalcare.server.application.admin.request.AdminInquiryMemoRequest
import com.mymentalcare.server.application.admin.request.AdminInquirySearchRequest
import com.mymentalcare.server.application.admin.request.AdminInquiryStatusChangeRequest
import com.mymentalcare.server.bootstrap.admin.web.request.AdminInquiryMemoPayload
import com.mymentalcare.server.bootstrap.admin.web.request.AdminInquiryStatusChangePayload
import com.mymentalcare.server.bootstrap.admin.web.response.AdminInquiryDetailResponse
import com.mymentalcare.server.bootstrap.admin.web.response.AdminInquiryPageResponse
import com.mymentalcare.server.domain.inquiry.InquiryStatus
import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/inquiries")
class AdminInquiryController(
    private val adminInquiryQueryInputPort: AdminInquiryQueryInputPort,
    private val adminInquiryCommandInputPort: AdminInquiryCommandInputPort,
) {
    @Operation(summary = "관리자 문의 목록 조회", description = "관리자가 사용자 문의를 상태와 검색어로 조회합니다.")
    @GetMapping
    fun readInquiries(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) status: InquiryStatus?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): AdminInquiryPageResponse {
        return adminInquiryQueryInputPort.readInquiries(
            AdminInquirySearchRequest(keyword = keyword, status = status, page = page, size = size),
        ).toBootstrapResponse()
    }

    @Operation(summary = "관리자 문의 상세 조회", description = "관리자가 문의 상세 내용과 운영 메모를 확인합니다.")
    @GetMapping("/{inquiryId}")
    fun readInquiry(
        @AuthenticationPrincipal adminMemberId: Long,
        @PathVariable inquiryId: Long,
    ): AdminInquiryDetailResponse {
        return adminInquiryQueryInputPort.readInquiry(adminMemberId = adminMemberId, inquiryId = inquiryId).toBootstrapResponse()
    }

    @Operation(summary = "관리자 문의 상태 변경", description = "관리자가 문의 처리 상태와 운영 메모를 저장합니다.")
    @PatchMapping("/{inquiryId}/status")
    fun changeInquiryStatus(
        @AuthenticationPrincipal adminMemberId: Long,
        @PathVariable inquiryId: Long,
        @RequestBody payload: AdminInquiryStatusChangePayload,
    ): AdminInquiryDetailResponse {
        return adminInquiryCommandInputPort.changeInquiryStatus(
            AdminInquiryStatusChangeRequest(
                adminMemberId = adminMemberId,
                inquiryId = inquiryId,
                status = payload.status,
                adminMemo = payload.adminMemo,
            ),
        ).toBootstrapResponse()
    }

    @Operation(summary = "관리자 문의 운영 메모 저장", description = "관리자가 문의 처리용 운영 메모만 별도로 저장합니다.")
    @PatchMapping("/{inquiryId}/memo")
    fun updateInquiryMemo(
        @AuthenticationPrincipal adminMemberId: Long,
        @PathVariable inquiryId: Long,
        @RequestBody payload: AdminInquiryMemoPayload,
    ): AdminInquiryDetailResponse {
        return adminInquiryCommandInputPort.updateInquiryMemo(
            AdminInquiryMemoRequest(
                adminMemberId = adminMemberId,
                inquiryId = inquiryId,
                adminMemo = payload.adminMemo,
            ),
        ).toBootstrapResponse()
    }
}

private fun com.mymentalcare.server.application.admin.response.AdminInquiryPageResponse.toBootstrapResponse(): AdminInquiryPageResponse {
    return AdminInquiryPageResponse(
        inquiries = inquiries.map {
            com.mymentalcare.server.bootstrap.admin.web.response.AdminInquirySummaryResponse(
                id = it.id,
                memberId = it.memberId,
                category = it.category,
                status = it.status,
                createdAt = it.createdAt,
            )
        },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
}

private fun com.mymentalcare.server.application.admin.response.AdminInquiryDetailResponse.toBootstrapResponse(): AdminInquiryDetailResponse {
    return AdminInquiryDetailResponse(
        id = id,
        memberId = memberId,
        category = category,
        content = content,
        status = status,
        adminMemo = adminMemo,
        handledByMemberId = handledByMemberId,
        handledAt = handledAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
