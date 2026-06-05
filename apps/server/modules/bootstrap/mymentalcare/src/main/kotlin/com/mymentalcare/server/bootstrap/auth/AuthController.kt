package com.mymentalcare.server.bootstrap.auth

import com.mymentalcare.server.application.auth.AuthenticationInputPort
import com.mymentalcare.server.application.auth.ReissueTokenRequest as ApplicationReissueTokenRequest
import com.mymentalcare.server.application.auth.SignInRequest
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
    private val authenticationInputPort: AuthenticationInputPort,
) {
    @Operation(
        summary = "로그인",
        description = "로그인 ID 또는 이메일과 비밀번호로 로그인하고 access token과 refresh token을 발급합니다.",
    )
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val response = authenticationInputPort.signIn(
            SignInRequest(
                identifier = request.identifier,
                password = request.password,
            )
        )

        return ResponseEntity.ok(
            LoginResponse(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                tokenType = response.tokenType,
                expiresInSeconds = response.expiresInSeconds,
            )
        )
    }

    @Operation(
        summary = "토큰 재발급",
        description = "유효한 refresh token을 검증하고 새로운 access token과 refresh token을 재발급합니다.",
    )
    @PostMapping("/reissue")
    fun reissue(
        @Valid @RequestBody request: ReissueTokenRequest,
    ): ResponseEntity<LoginResponse> {
        val response = authenticationInputPort.reissue(
            ApplicationReissueTokenRequest(
                refreshToken = request.refreshToken,
            )
        )

        return ResponseEntity.ok(
            LoginResponse(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                tokenType = response.tokenType,
                expiresInSeconds = response.expiresInSeconds,
            )
        )
    }
}
