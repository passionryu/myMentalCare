package com.mymentalcare.server.bootstrap.auth

import com.mymentalcare.server.application.auth.LoginCommand
import com.mymentalcare.server.application.auth.LoginUseCase
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val loginUseCase: LoginUseCase,
) {
    @Operation(
        summary = "로그인",
        description = "로그인 ID 또는 이메일과 비밀번호로 로그인하고 access token과 refresh token을 발급합니다.",
    )
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val result = loginUseCase.loginMember(
            LoginCommand(
                identifier = request.identifier,
                password = request.password,
            )
        )

        return ResponseEntity.ok(
            LoginResponse(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
                tokenType = result.tokenType,
                expiresInSeconds = result.expiresInSeconds,
            )
        )
    }
}
