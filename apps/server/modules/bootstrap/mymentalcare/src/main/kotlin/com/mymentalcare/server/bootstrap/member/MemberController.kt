package com.mymentalcare.server.bootstrap.member

import com.mymentalcare.server.application.member.MemberInputPort
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberInputPort: MemberInputPort,
) {
    @Operation(
        summary = "내 프로필 조회",
        description = "로그인한 사용자의 이름, 로그인 아이디, 이메일, 전화번호를 조회합니다.",
    )
    @GetMapping("/me")
    fun readMyProfile(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<MyProfileResponse> {
        val response = memberInputPort.readMyProfile(memberId)

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
