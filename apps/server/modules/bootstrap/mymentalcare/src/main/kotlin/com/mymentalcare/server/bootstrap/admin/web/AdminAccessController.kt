package com.mymentalcare.server.bootstrap.admin.web

import com.mymentalcare.server.application.admin.port.AdminAccessInputPort
import com.mymentalcare.server.bootstrap.admin.web.response.AdminProfileResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminAccessController(
    private val adminAccessInputPort: AdminAccessInputPort,
) {
    @Operation(
        summary = "관리자 프로필 조회",
        description = "관리자 페이지 진입 시 현재 로그인한 관리자의 기본 정보와 권한을 확인합니다.",
    )
    @GetMapping("/me")
    fun readAdminProfile(
        @AuthenticationPrincipal memberId: Long,
    ): AdminProfileResponse {
        return adminAccessInputPort.readAdminProfile(memberId).toBootstrapResponse()
    }
}

private fun com.mymentalcare.server.application.admin.response.AdminProfileResponse.toBootstrapResponse(): AdminProfileResponse {
    return AdminProfileResponse(
        memberId = memberId,
        loginId = loginId,
        name = name,
        role = role,
    )
}
