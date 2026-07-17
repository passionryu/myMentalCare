package com.mymentalcare.server.application.admin.usecase

import com.mymentalcare.server.application.admin.AdminMemberInvalidStatusException
import com.mymentalcare.server.application.admin.AdminMemberNotFoundException
import com.mymentalcare.server.application.admin.port.AdminMemberCommandInputPort
import com.mymentalcare.server.application.admin.port.AdminMemberPage
import com.mymentalcare.server.application.admin.port.AdminMemberQueryInputPort
import com.mymentalcare.server.application.admin.port.AdminMemberRecord
import com.mymentalcare.server.application.admin.port.AdminMemberRepository
import com.mymentalcare.server.application.admin.recorder.AdminAuditLogRecorder
import com.mymentalcare.server.application.admin.request.AdminAuditLogRecordRequest
import com.mymentalcare.server.application.admin.request.AdminMemberSearchRequest
import com.mymentalcare.server.application.admin.request.AdminMemberStatusChangeRequest
import com.mymentalcare.server.application.admin.response.AdminMemberDetailResponse
import com.mymentalcare.server.application.admin.response.AdminMemberPageResponse
import com.mymentalcare.server.application.admin.response.AdminMemberSummaryResponse
import com.mymentalcare.server.application.member.MemberNotFoundException
import com.mymentalcare.server.application.member.port.MemberRepository
import com.mymentalcare.server.domain.admin.AdminAuditLogAction
import com.mymentalcare.server.domain.admin.AdminAuditLogTargetType
import com.mymentalcare.server.domain.member.MemberRole
import com.mymentalcare.server.domain.member.MemberStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminMemberService(
    private val adminMemberRepository: AdminMemberRepository,
    private val memberRepository: MemberRepository,
    private val adminAuditLogRecorder: AdminAuditLogRecorder,
) : AdminMemberQueryInputPort, AdminMemberCommandInputPort {
    @Transactional(readOnly = true)
    override fun readMembers(request: AdminMemberSearchRequest): AdminMemberPageResponse {
        val normalizedRequest = request.normalized()

        return adminMemberRepository.findMembers(
            keyword = normalizedRequest.keyword,
            status = normalizedRequest.status,
            page = normalizedRequest.page,
            size = normalizedRequest.size,
        ).toPageResponse()
    }

    @Transactional
    override fun readMember(adminMemberId: Long, memberId: Long): AdminMemberDetailResponse {
        val adminMember = memberRepository.findById(adminMemberId) ?: throw MemberNotFoundException()
        val member = adminMemberRepository.findMemberById(memberId) ?: throw AdminMemberNotFoundException()

        adminAuditLogRecorder.record(
            AdminAuditLogRecordRequest(
                adminMemberId = adminMember.id,
                adminLoginId = adminMember.loginId,
                action = AdminAuditLogAction.MEMBER_VIEW,
                targetType = AdminAuditLogTargetType.MEMBER,
                targetId = member.id,
                reason = "관리자 회원 상세 조회",
            ),
        )

        return member.toDetailResponse()
    }

    @Transactional
    override fun changeMemberStatus(request: AdminMemberStatusChangeRequest): AdminMemberDetailResponse {
        val adminMember = memberRepository.findById(request.adminMemberId) ?: throw MemberNotFoundException()
        val reason = request.reason.trim()
        if (reason.isBlank()) {
            throw AdminMemberInvalidStatusException("회원 상태 변경 사유가 필요합니다.")
        }

        val currentMember = adminMemberRepository.findMemberById(request.memberId) ?: throw AdminMemberNotFoundException()
        validateStatusChange(currentMember, request.status)

        val changedMember = adminMemberRepository.changeStatus(currentMember.id, request.status)
            ?: throw AdminMemberNotFoundException()

        adminAuditLogRecorder.record(
            AdminAuditLogRecordRequest(
                adminMemberId = adminMember.id,
                adminLoginId = adminMember.loginId,
                action = AdminAuditLogAction.MEMBER_STATUS_CHANGE,
                targetType = AdminAuditLogTargetType.MEMBER,
                targetId = changedMember.id,
                reason = "${currentMember.status.name} -> ${changedMember.status.name}: $reason",
            ),
        )

        return changedMember.toDetailResponse()
    }

    private fun validateStatusChange(member: AdminMemberRecord, nextStatus: MemberStatus) {
        if (member.role == MemberRole.ADMIN) {
            throw AdminMemberInvalidStatusException("관리자 계정의 상태는 이 화면에서 변경할 수 없습니다.")
        }

        if (member.status == nextStatus) {
            throw AdminMemberInvalidStatusException("이미 ${nextStatus.name} 상태입니다.")
        }

        if (member.status == MemberStatus.WITHDRAWN) {
            throw AdminMemberInvalidStatusException("탈퇴 처리된 회원은 상태를 다시 변경할 수 없습니다.")
        }
    }
}

private fun AdminMemberPage.toPageResponse(): AdminMemberPageResponse {
    return AdminMemberPageResponse(
        members = members.map { it.toSummaryResponse() },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
}

private fun AdminMemberRecord.toSummaryResponse(): AdminMemberSummaryResponse {
    return AdminMemberSummaryResponse(
        id = id,
        loginId = loginId,
        email = email,
        name = name,
        status = status.name,
        role = role.name,
        createdAt = createdAt,
    )
}

private fun AdminMemberRecord.toDetailResponse(): AdminMemberDetailResponse {
    return AdminMemberDetailResponse(
        id = id,
        loginId = loginId,
        email = email,
        name = name,
        phone = phone,
        status = status.name,
        role = role.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
}
