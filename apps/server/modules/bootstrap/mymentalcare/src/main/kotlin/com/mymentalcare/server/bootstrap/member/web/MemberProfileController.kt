package com.mymentalcare.server.bootstrap.member.web

import com.mymentalcare.server.application.member.port.MemberProfileInputPort
import com.mymentalcare.server.application.member.request.UpdateMyProfileRequest
import com.mymentalcare.server.bootstrap.member.web.request.MemberUpdateMyProfileRequest
import com.mymentalcare.server.bootstrap.member.web.response.MyProfileResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberProfileController(
    private val memberProfileInputPort: MemberProfileInputPort,
) {
    @Operation(
        summary = "내 프로필 조회",
        description = "로그인한 사용자의 이름, 로그인 아이디, 이메일, 전화번호를 조회합니다.",
    )
    @GetMapping("/me")
    fun readMyProfile(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<MyProfileResponse> {
        val response = memberProfileInputPort.readMyProfile(memberId)

        return ResponseEntity.ok(
            MyProfileResponse(
                memberId = response.memberId,
                loginId = response.loginId,
                email = response.email,
                name = response.name,
                phone = response.phone,
            )
        )
    }

    @Operation(
        summary = "내 프로필 수정",
        description = "로그인한 사용자의 이름, 이메일, 전화번호를 수정합니다. 로그인 아이디와 비밀번호는 수정하지 않습니다.",
    )
    @PatchMapping("/me")
    fun updateMyProfile(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody request: MemberUpdateMyProfileRequest,
    ): ResponseEntity<MyProfileResponse> {
        val response = memberProfileInputPort.updateMyProfile(
            memberId = memberId,
            request = UpdateMyProfileRequest(
                name = request.name,
                email = request.email,
                phone = request.phone,
            ),
        )

        return ResponseEntity.ok(
            MyProfileResponse(
                memberId = response.memberId,
                loginId = response.loginId,
                email = response.email,
                name = response.name,
                phone = response.phone,
            )
        )
    }
}
