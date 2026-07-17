package com.mymentalcare.server.bootstrap.admin.web

import com.mymentalcare.server.application.admin.port.AdminMemberCommandInputPort
import com.mymentalcare.server.application.admin.port.AdminMemberQueryInputPort
import com.mymentalcare.server.application.admin.request.AdminMemberSearchRequest
import com.mymentalcare.server.application.admin.request.AdminMemberStatusChangeRequest
import com.mymentalcare.server.bootstrap.admin.web.request.AdminMemberStatusChangePayload
import com.mymentalcare.server.bootstrap.admin.web.response.AdminMemberDetailResponse
import com.mymentalcare.server.bootstrap.admin.web.response.AdminMemberPageResponse
import com.mymentalcare.server.domain.member.MemberStatus
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
@RequestMapping("/api/admin/users")
class AdminMemberController(
    private val adminMemberQueryInputPort: AdminMemberQueryInputPort,
    private val adminMemberCommandInputPort: AdminMemberCommandInputPort,
) {
    @Operation(
        summary = "관리자 회원 목록 조회",
        description = "관리자가 회원을 검색하고 상태별로 필터링합니다.",
    )
    @GetMapping
    fun readMembers(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) status: MemberStatus?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): AdminMemberPageResponse {
        return adminMemberQueryInputPort.readMembers(
            AdminMemberSearchRequest(
                keyword = keyword,
                status = status,
                page = page,
                size = size,
            ),
        ).toBootstrapResponse()
    }

    @Operation(
        summary = "관리자 회원 상세 조회",
        description = "관리자가 특정 회원의 기본 정보와 상태를 확인합니다.",
    )
    @GetMapping("/{memberId}")
    fun readMember(
        @AuthenticationPrincipal adminMemberId: Long,
        @PathVariable memberId: Long,
    ): AdminMemberDetailResponse {
        return adminMemberQueryInputPort.readMember(adminMemberId = adminMemberId, memberId = memberId).toBootstrapResponse()
    }

    @Operation(
        summary = "관리자 회원 상태 변경",
        description = "관리자가 회원을 정지, 정지 해제 또는 탈퇴 처리합니다. 상태 변경 사유가 필요합니다.",
    )
    @PatchMapping("/{memberId}/status")
    fun changeMemberStatus(
        @AuthenticationPrincipal adminMemberId: Long,
        @PathVariable memberId: Long,
        @RequestBody payload: AdminMemberStatusChangePayload,
    ): AdminMemberDetailResponse {
        return adminMemberCommandInputPort.changeMemberStatus(
            AdminMemberStatusChangeRequest(
                adminMemberId = adminMemberId,
                memberId = memberId,
                status = payload.status,
                reason = payload.reason,
            ),
        ).toBootstrapResponse()
    }
}

private fun com.mymentalcare.server.application.admin.response.AdminMemberPageResponse.toBootstrapResponse(): AdminMemberPageResponse {
    return AdminMemberPageResponse(
        members = members.map {
            com.mymentalcare.server.bootstrap.admin.web.response.AdminMemberSummaryResponse(
                id = it.id,
                loginId = it.loginId,
                email = it.email,
                name = it.name,
                status = it.status,
                role = it.role,
                createdAt = it.createdAt,
            )
        },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
}

private fun com.mymentalcare.server.application.admin.response.AdminMemberDetailResponse.toBootstrapResponse(): AdminMemberDetailResponse {
    return AdminMemberDetailResponse(
        id = id,
        loginId = loginId,
        email = email,
        name = name,
        phone = phone,
        status = status,
        role = role,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
}
