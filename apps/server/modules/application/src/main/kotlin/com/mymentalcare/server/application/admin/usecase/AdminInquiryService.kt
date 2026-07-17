package com.mymentalcare.server.application.admin.usecase

import com.mymentalcare.server.application.admin.AdminInquiryInvalidRequestException
import com.mymentalcare.server.application.admin.AdminInquiryNotFoundException
import com.mymentalcare.server.application.admin.port.AdminInquiryCommandInputPort
import com.mymentalcare.server.application.admin.port.AdminInquiryPage
import com.mymentalcare.server.application.admin.port.AdminInquiryQueryInputPort
import com.mymentalcare.server.application.admin.port.AdminInquiryRecord
import com.mymentalcare.server.application.admin.port.AdminInquiryRepository
import com.mymentalcare.server.application.admin.recorder.AdminAuditLogRecorder
import com.mymentalcare.server.application.admin.request.AdminAuditLogRecordRequest
import com.mymentalcare.server.application.admin.request.AdminInquiryMemoRequest
import com.mymentalcare.server.application.admin.request.AdminInquirySearchRequest
import com.mymentalcare.server.application.admin.request.AdminInquiryStatusChangeRequest
import com.mymentalcare.server.application.admin.response.AdminInquiryDetailResponse
import com.mymentalcare.server.application.admin.response.AdminInquiryPageResponse
import com.mymentalcare.server.application.admin.response.AdminInquirySummaryResponse
import com.mymentalcare.server.application.member.MemberNotFoundException
import com.mymentalcare.server.application.member.port.MemberRepository
import com.mymentalcare.server.domain.admin.AdminAuditLogAction
import com.mymentalcare.server.domain.admin.AdminAuditLogTargetType
import com.mymentalcare.server.domain.inquiry.InquiryStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminInquiryService(
    private val adminInquiryRepository: AdminInquiryRepository,
    private val memberRepository: MemberRepository,
    private val adminAuditLogRecorder: AdminAuditLogRecorder,
) : AdminInquiryQueryInputPort, AdminInquiryCommandInputPort {
    @Transactional(readOnly = true)
    override fun readInquiries(request: AdminInquirySearchRequest): AdminInquiryPageResponse {
        val normalizedRequest = request.normalized()

        return adminInquiryRepository.findInquiries(
            keyword = normalizedRequest.keyword,
            status = normalizedRequest.status,
            page = normalizedRequest.page,
            size = normalizedRequest.size,
        ).toPageResponse()
    }

    @Transactional
    override fun readInquiry(adminMemberId: Long, inquiryId: Long): AdminInquiryDetailResponse {
        val adminMember = memberRepository.findById(adminMemberId) ?: throw MemberNotFoundException()
        val inquiry = adminInquiryRepository.findInquiryById(inquiryId) ?: throw AdminInquiryNotFoundException()

        adminAuditLogRecorder.record(
            AdminAuditLogRecordRequest(
                adminMemberId = adminMember.id,
                adminLoginId = adminMember.loginId,
                action = AdminAuditLogAction.INQUIRY_VIEW,
                targetType = AdminAuditLogTargetType.INQUIRY,
                targetId = inquiry.id,
                reason = "관리자 문의 상세 조회",
            ),
        )

        return inquiry.toDetailResponse()
    }

    @Transactional
    override fun changeInquiryStatus(request: AdminInquiryStatusChangeRequest): AdminInquiryDetailResponse {
        val adminMember = memberRepository.findById(request.adminMemberId) ?: throw MemberNotFoundException()
        val currentInquiry = adminInquiryRepository.findInquiryById(request.inquiryId) ?: throw AdminInquiryNotFoundException()
        val memo = request.adminMemo?.trim()?.takeIf { it.isNotBlank() }

        if (currentInquiry.status == request.status) {
            throw AdminInquiryInvalidRequestException("이미 ${request.status.name} 상태입니다.")
        }

        if (request.status == InquiryStatus.DONE && memo.isNullOrBlank()) {
            throw AdminInquiryInvalidRequestException("완료 처리 시 운영 메모가 필요합니다.")
        }

        val changedInquiry = adminInquiryRepository.changeStatus(
            inquiryId = request.inquiryId,
            status = request.status,
            adminMemo = memo ?: currentInquiry.adminMemo,
            handledByMemberId = adminMember.id,
        ) ?: throw AdminInquiryNotFoundException()

        adminAuditLogRecorder.record(
            AdminAuditLogRecordRequest(
                adminMemberId = adminMember.id,
                adminLoginId = adminMember.loginId,
                action = AdminAuditLogAction.INQUIRY_STATUS_CHANGE,
                targetType = AdminAuditLogTargetType.INQUIRY,
                targetId = changedInquiry.id,
                reason = "${currentInquiry.status.name} -> ${changedInquiry.status.name}",
            ),
        )

        return changedInquiry.toDetailResponse()
    }

    @Transactional
    override fun updateInquiryMemo(request: AdminInquiryMemoRequest): AdminInquiryDetailResponse {
        val adminMember = memberRepository.findById(request.adminMemberId) ?: throw MemberNotFoundException()
        val memo = request.adminMemo?.trim()?.takeIf { it.isNotBlank() }
        val changedInquiry = adminInquiryRepository.updateMemo(request.inquiryId, memo) ?: throw AdminInquiryNotFoundException()

        adminAuditLogRecorder.record(
            AdminAuditLogRecordRequest(
                adminMemberId = adminMember.id,
                adminLoginId = adminMember.loginId,
                action = AdminAuditLogAction.INQUIRY_MEMO_CHANGE,
                targetType = AdminAuditLogTargetType.INQUIRY,
                targetId = changedInquiry.id,
                reason = "관리자 문의 운영 메모 수정",
            ),
        )

        return changedInquiry.toDetailResponse()
    }
}

private fun AdminInquiryPage.toPageResponse(): AdminInquiryPageResponse {
    return AdminInquiryPageResponse(
        inquiries = inquiries.map { it.toSummaryResponse() },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
}

private fun AdminInquiryRecord.toSummaryResponse(): AdminInquirySummaryResponse {
    return AdminInquirySummaryResponse(
        id = id,
        memberId = memberId,
        category = category,
        status = status.name,
        createdAt = createdAt,
    )
}

private fun AdminInquiryRecord.toDetailResponse(): AdminInquiryDetailResponse {
    return AdminInquiryDetailResponse(
        id = id,
        memberId = memberId,
        category = category,
        content = content,
        status = status.name,
        adminMemo = adminMemo,
        handledByMemberId = handledByMemberId,
        handledAt = handledAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
