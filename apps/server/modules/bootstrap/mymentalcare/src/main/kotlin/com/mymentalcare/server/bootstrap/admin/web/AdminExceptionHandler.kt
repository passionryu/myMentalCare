package com.mymentalcare.server.bootstrap.admin.web

import com.mymentalcare.server.application.admin.AdminAccessDeniedException
import com.mymentalcare.server.application.admin.AdminInquiryInvalidRequestException
import com.mymentalcare.server.application.admin.AdminInquiryNotFoundException
import com.mymentalcare.server.application.admin.AdminChatRoomNotFoundException
import com.mymentalcare.server.application.admin.AdminMemberInvalidStatusException
import com.mymentalcare.server.application.admin.AdminMemberNotFoundException
import com.mymentalcare.server.application.admin.AdminIncidentInvalidRequestException
import com.mymentalcare.server.application.admin.AdminReportNotFoundException
import com.mymentalcare.server.application.admin.AdminSensitiveAccessReasonRequiredException
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

    @ExceptionHandler(AdminInquiryNotFoundException::class)
    fun handleAdminInquiryNotFound(exception: AdminInquiryNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(code = "INQUIRY_NOT_FOUND", message = exception.message ?: "문의를 찾을 수 없습니다."))
    }

    @ExceptionHandler(AdminInquiryInvalidRequestException::class)
    fun handleAdminInquiryInvalidRequest(exception: AdminInquiryInvalidRequestException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(code = "INVALID_INQUIRY_REQUEST", message = exception.message ?: "문의 처리 요청이 올바르지 않습니다."))
    }

    @ExceptionHandler(AdminReportNotFoundException::class)
    fun handleAdminReportNotFound(exception: AdminReportNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(code = "REPORT_NOT_FOUND", message = exception.message ?: "리포트를 찾을 수 없습니다."))
    }

    @ExceptionHandler(AdminChatRoomNotFoundException::class)
    fun handleAdminChatRoomNotFound(exception: AdminChatRoomNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(code = "CHAT_ROOM_NOT_FOUND", message = exception.message ?: "대화방을 찾을 수 없습니다."))
    }

    @ExceptionHandler(AdminSensitiveAccessReasonRequiredException::class)
    fun handleAdminSensitiveAccessReasonRequired(exception: AdminSensitiveAccessReasonRequiredException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(code = "REASON_REQUIRED", message = exception.message ?: "조회 사유가 필요합니다."))
    }

    @ExceptionHandler(AdminIncidentInvalidRequestException::class)
    fun handleAdminIncidentInvalidRequest(exception: AdminIncidentInvalidRequestException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(code = "INVALID_INCIDENT_REQUEST", message = exception.message ?: "장애 기록 요청이 올바르지 않습니다."))
    }
}
