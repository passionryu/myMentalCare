package com.mymentalcare.server.bootstrap.member

import com.mymentalcare.server.application.member.MemberNotFoundException
import com.mymentalcare.server.bootstrap.auth.ApiErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class MemberExceptionHandler {
    @ExceptionHandler(MemberNotFoundException::class)
    fun handleMemberNotFound(exception: MemberNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(code = "MEMBER_NOT_FOUND", message = exception.message ?: "회원 정보를 찾을 수 없습니다."))
    }
}
