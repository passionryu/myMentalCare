package com.mymentalcare.server.bootstrap.admin.web

import com.mymentalcare.server.application.admin.AdminAccessDeniedException
import com.mymentalcare.server.application.admin.AdminMemberInvalidStatusException
import com.mymentalcare.server.application.admin.AdminMemberNotFoundException
import com.mymentalcare.server.bootstrap.common.web.ApiErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AdminExceptionHandler {
    @ExceptionHandler(AdminAccessDeniedException::class)
    fun handleAdminAccessDenied(exception: AdminAccessDeniedException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiErrorResponse(code = "ADMIN_ACCESS_DENIED", message = exception.message ?: "관리자 권한이 필요합니다."))
    }

    @ExceptionHandler(AdminMemberNotFoundException::class)
    fun handleAdminMemberNotFound(exception: AdminMemberNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(code = "MEMBER_NOT_FOUND", message = exception.message ?: "회원을 찾을 수 없습니다."))
    }

    @ExceptionHandler(AdminMemberInvalidStatusException::class)
    fun handleAdminMemberInvalidStatus(exception: AdminMemberInvalidStatusException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(code = "INVALID_STATUS", message = exception.message ?: "변경할 수 없는 회원 상태입니다."))
    }
}
