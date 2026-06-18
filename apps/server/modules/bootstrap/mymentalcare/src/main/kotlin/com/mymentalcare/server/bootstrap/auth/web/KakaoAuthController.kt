package com.mymentalcare.server.bootstrap.auth.web

import com.mymentalcare.server.bootstrap.auth.web.request.*
import com.mymentalcare.server.bootstrap.auth.web.response.*
import com.mymentalcare.server.bootstrap.common.web.ApiErrorResponse

import com.mymentalcare.server.application.auth.KakaoAccountConflictException
import com.mymentalcare.server.application.auth.KakaoAuthFailedException
import com.mymentalcare.server.application.auth.port.KakaoAuthenticationInputPort
import com.mymentalcare.server.application.auth.request.KakaoCallbackRequest
import com.mymentalcare.server.application.auth.request.KakaoExchangeRequest as ApplicationKakaoExchangeRequest
import com.mymentalcare.server.application.auth.request.KakaoLoginStartRequest
import com.mymentalcare.server.application.auth.OAuthStateInvalidException
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/auth/kakao")
class KakaoAuthController(
    private val kakaoAuthenticationInputPort: KakaoAuthenticationInputPort,
    private val redirectResponseFactory: KakaoOAuthRedirectResponseFactory,
) {
    @Operation(
        summary = "카카오 로그인 시작",
        description = "카카오 OAuth 인증 화면으로 이동하기 위한 redirect 응답을 반환합니다.",
    )
    @GetMapping("/login")
    fun login(
        @RequestParam(required = false) redirectTo: String?,
    ): ResponseEntity<Void> {
        val response = try {
            kakaoAuthenticationInputPort.startLogin(KakaoLoginStartRequest(redirectTo = redirectTo))
        } catch (e: KakaoAuthFailedException) {
            return redirectResponseFactory.redirectToWebCallback(errorCode = "KAKAO_AUTH_FAILED")
        }

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(response.authorizationUrl))
            .build()
    }

    @Operation(
        summary = "카카오 로그인 콜백",
        description = "카카오 인가 코드를 처리하고 프론트 callback 화면으로 one-time code를 전달합니다.",
    )
    @GetMapping("/callback")
    fun callback(
        @RequestParam(required = false) code: String?,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false) error: String?,
    ): ResponseEntity<Void> {
        if (!error.isNullOrBlank()) {
            return redirectResponseFactory.redirectToWebCallback(errorCode = "KAKAO_AUTH_CANCELLED")
        }
        if (code.isNullOrBlank() || state.isNullOrBlank()) {
            return redirectResponseFactory.redirectToWebCallback(errorCode = "KAKAO_AUTH_FAILED")
        }

        return try {
            val response = kakaoAuthenticationInputPort.handleCallback(
                KakaoCallbackRequest(
                    code = code,
                    state = state,
                )
            )
            redirectResponseFactory.redirectToWebCallback(
                code = response.oneTimeCode,
                redirectTo = response.redirectTo,
            )
        } catch (e: OAuthStateInvalidException) {
            redirectResponseFactory.redirectToWebCallback(errorCode = "KAKAO_STATE_INVALID")
        } catch (e: KakaoAccountConflictException) {
            redirectResponseFactory.redirectToWebCallback(errorCode = "KAKAO_ACCOUNT_CONFLICT")
        } catch (e: KakaoAuthFailedException) {
            redirectResponseFactory.redirectToWebCallback(errorCode = "KAKAO_AUTH_FAILED")
        }
    }

    @Operation(
        summary = "카카오 로그인 결과 교환",
        description = "프론트 callback 화면이 one-time code를 기존 JWT 로그인 응답으로 교환합니다.",
    )
    @PostMapping("/exchange")
    fun exchange(
        @Valid @RequestBody request: KakaoExchangeRequest,
    ): ResponseEntity<LoginResponse> {
        val response = kakaoAuthenticationInputPort.exchange(
            ApplicationKakaoExchangeRequest(code = request.code)
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
