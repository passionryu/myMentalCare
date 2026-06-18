package com.mymentalcare.server.bootstrap.member.web

import com.mymentalcare.server.application.member.port.MemberSecurityInputPort
import com.mymentalcare.server.application.member.request.ChangeMemberPasswordRequest
import com.mymentalcare.server.application.member.request.WithdrawMemberRequest
import com.mymentalcare.server.bootstrap.member.web.request.MemberLoginMethodsResponse
import com.mymentalcare.server.bootstrap.member.web.request.MemberPasswordChangeResponse
import com.mymentalcare.server.bootstrap.member.web.request.MemberPasswordChangePayload
import com.mymentalcare.server.bootstrap.member.web.request.MemberSocialAccountResponse
import com.mymentalcare.server.bootstrap.member.web.request.MemberWithdrawalPayload
import com.mymentalcare.server.bootstrap.member.web.request.MemberWithdrawalResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberSecurityController(
    private val memberSecurityInputPort: MemberSecurityInputPort,
) {
    @Operation(
        summary = "회원 탈퇴",
        description = "로그인한 사용자가 비밀번호와 확인 문구를 입력해 계정을 탈퇴 처리합니다.",
    )
    @DeleteMapping("/me")
    fun withdrawMyAccount(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody payload: MemberWithdrawalPayload,
    ): ResponseEntity<MemberWithdrawalResponse> {
        val response = memberSecurityInputPort.withdrawMyAccount(
            memberId = memberId,
            request = WithdrawMemberRequest(
                password = payload.password,
                confirmationText = payload.confirmationText,
            ),
        )

        return ResponseEntity.ok(MemberWithdrawalResponse(withdrawn = response.withdrawn))
    }

    @Operation(
        summary = "내 로그인 방식 조회",
        description = "현재 계정의 일반 로그인 사용 가능 여부와 연결된 소셜 로그인 계정을 조회합니다.",
    )
    @GetMapping("/me/login-methods")
    fun readLoginMethods(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<MemberLoginMethodsResponse> {
        val response = memberSecurityInputPort.readLoginMethods(memberId)

        return ResponseEntity.ok(
            MemberLoginMethodsResponse(
                passwordLoginEnabled = response.passwordLoginEnabled,
                canChangePassword = response.canChangePassword,
                socialAccounts = response.socialAccounts.map {
                    MemberSocialAccountResponse(
                        provider = it.provider,
                        email = it.email,
                        linkedAt = it.linkedAt,
                    )
                },
            )
        )
    }

    @Operation(
        summary = "내 비밀번호 변경",
        description = "현재 비밀번호를 확인한 뒤 새 비밀번호로 변경합니다.",
    )
    @PatchMapping("/me/password")
    fun changePassword(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody payload: MemberPasswordChangePayload,
    ): ResponseEntity<MemberPasswordChangeResponse> {
        val response = memberSecurityInputPort.changePassword(
            memberId = memberId,
            request = ChangeMemberPasswordRequest(
                currentPassword = payload.currentPassword,
                newPassword = payload.newPassword,
            ),
        )

        return ResponseEntity.ok(MemberPasswordChangeResponse(changed = response.changed))
    }
}
