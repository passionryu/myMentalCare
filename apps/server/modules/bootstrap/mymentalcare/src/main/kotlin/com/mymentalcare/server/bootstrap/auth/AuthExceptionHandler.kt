package com.mymentalcare.server.bootstrap.auth

import com.mymentalcare.server.application.auth.LoginFailedException
import com.mymentalcare.server.application.auth.KakaoAccountConflictException
import com.mymentalcare.server.application.auth.KakaoAuthFailedException
import com.mymentalcare.server.application.auth.OAuthExchangeCodeInvalidException
import com.mymentalcare.server.application.auth.OAuthStateInvalidException
import com.mymentalcare.server.application.auth.TokenReissueFailedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {
    @ExceptionHandler(LoginFailedException::class)
    fun handleLoginFailed(exception: LoginFailedException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponse(code = "AUTH_LOGIN_FAILED", message = exception.message ?: "로그인 정보를 다시 확인해주세요."))
    }

    @ExceptionHandler(TokenReissueFailedException::class)
    fun handleTokenReissueFailed(exception: TokenReissueFailedException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponse(code = "AUTH_REISSUE_FAILED", message = exception.message ?: "로그인이 만료되었습니다. 다시 로그인해주세요."))
    }

    @ExceptionHandler(KakaoAccountConflictException::class)
    fun handleKakaoAccountConflict(exception: KakaoAccountConflictException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(code = "KAKAO_ACCOUNT_CONFLICT", message = exception.message ?: "이미 같은 이메일로 가입된 계정이 있습니다."))
    }

    @ExceptionHandler(KakaoAuthFailedException::class)
    fun handleKakaoAuthFailed(exception: KakaoAuthFailedException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(ApiErrorResponse(code = "KAKAO_AUTH_FAILED", message = exception.message ?: "카카오 로그인 처리 중 문제가 발생했습니다."))
    }

    @ExceptionHandler(OAuthStateInvalidException::class)
    fun handleOAuthStateInvalid(exception: OAuthStateInvalidException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .badRequest()
            .body(ApiErrorResponse(code = "KAKAO_STATE_INVALID", message = exception.message ?: "로그인 요청 시간이 만료되었습니다."))
    }

    @ExceptionHandler(OAuthExchangeCodeInvalidException::class)
    fun handleOAuthExchangeCodeInvalid(exception: OAuthExchangeCodeInvalidException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .badRequest()
            .body(ApiErrorResponse(code = "KAKAO_EXCHANGE_CODE_INVALID", message = exception.message ?: "카카오 로그인 결과를 확인할 수 없습니다."))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalidRequest(exception: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .badRequest()
            .body(ApiErrorResponse(code = "AUTH_INVALID_REQUEST", message = "입력한 로그인 정보를 다시 확인해주세요."))
    }
}
