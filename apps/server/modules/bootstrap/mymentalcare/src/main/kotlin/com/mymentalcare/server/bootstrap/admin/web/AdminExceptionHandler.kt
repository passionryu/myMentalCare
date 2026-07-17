package com.mymentalcare.server.bootstrap.admin.web

import com.mymentalcare.server.application.admin.AdminAccessDeniedException
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
}
