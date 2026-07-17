package com.mymentalcare.server.application.admin.usecase

import com.mymentalcare.server.application.admin.AdminAccessDeniedException
import com.mymentalcare.server.application.admin.port.AdminAccessInputPort
import com.mymentalcare.server.application.admin.recorder.AdminAuditLogRecorder
import com.mymentalcare.server.application.admin.request.AdminAuditLogRecordRequest
import com.mymentalcare.server.application.admin.response.AdminProfileResponse
import com.mymentalcare.server.application.member.MemberNotFoundException
import com.mymentalcare.server.application.member.port.MemberRepository
import com.mymentalcare.server.domain.admin.AdminAuditLogAction
import com.mymentalcare.server.domain.admin.AdminAuditLogTargetType
import com.mymentalcare.server.domain.member.MemberRole
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminAccessService(
    private val memberRepository: MemberRepository,
    private val adminAuditLogRecorder: AdminAuditLogRecorder,
) : AdminAccessInputPort {
    @Transactional
    override fun readAdminProfile(memberId: Long): AdminProfileResponse {
        val member = memberRepository.findById(memberId)
            ?: throw MemberNotFoundException()

        if (member.role != MemberRole.ADMIN) {
            throw AdminAccessDeniedException()
        }

        adminAuditLogRecorder.record(
            AdminAuditLogRecordRequest(
                adminMemberId = member.id,
                adminLoginId = member.loginId,
                action = AdminAuditLogAction.ADMIN_CONSOLE_ACCESS,
                targetType = AdminAuditLogTargetType.ADMIN,
                targetId = member.id,
                reason = "관리자 콘솔 접근",
            ),
        )

        return AdminProfileResponse(
            memberId = member.id,
            loginId = member.loginId,
            name = member.name,
            role = member.role.name,
        )
    }
}
