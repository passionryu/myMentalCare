package com.mymentalcare.server.bootstrap.auth

import com.mymentalcare.server.application.auth.LoginFailedException
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

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalidRequest(exception: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .badRequest()
            .body(ApiErrorResponse(code = "AUTH_INVALID_REQUEST", message = "입력한 로그인 정보를 다시 확인해주세요."))
    }
}
