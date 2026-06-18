package com.mymentalcare.server.bootstrap.member.web

import com.mymentalcare.server.application.member.port.MemberRegistrationInputPort
import com.mymentalcare.server.application.member.request.SignUpMemberRequest
import com.mymentalcare.server.bootstrap.member.web.request.MemberSignUpRequest
import com.mymentalcare.server.bootstrap.member.web.response.MemberSignUpResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberRegistrationController(
    private val memberRegistrationInputPort: MemberRegistrationInputPort,
) {
    @Operation(
        summary = "회원가입",
        description = "로그인 아이디, 이름, 비밀번호를 기반으로 새 회원을 생성합니다. 이메일과 전화번호는 선택 입력입니다.",
    )
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: MemberSignUpRequest,
    ): ResponseEntity<MemberSignUpResponse> {
        val response = memberRegistrationInputPort.signUp(
            SignUpMemberRequest(
                loginId = request.loginId,
                email = request.email,
                password = request.password,
                name = request.name,
                phone = request.phone,
            )
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                MemberSignUpResponse(
                    memberId = response.memberId,
                    loginId = response.loginId,
                    name = response.name,
                )
            )
    }
}
