package com.mymentalcare.server.bootstrap.member

import com.mymentalcare.server.application.member.DuplicateEmailException
import com.mymentalcare.server.application.member.DuplicateLoginIdException
import com.mymentalcare.server.application.member.MemberNotFoundException
import com.mymentalcare.server.bootstrap.auth.ApiErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class MemberExceptionHandler {
    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmail(exception: DuplicateEmailException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(code = "MEMBER_DUPLICATE_EMAIL", message = exception.message ?: "이미 사용 중인 이메일입니다."))
    }

    @ExceptionHandler(DuplicateLoginIdException::class)
    fun handleDuplicateLoginId(exception: DuplicateLoginIdException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(code = "MEMBER_DUPLICATE_LOGIN_ID", message = exception.message ?: "이미 존재하는 로그인 아이디입니다."))
    }

    @ExceptionHandler(MemberNotFoundException::class)
    fun handleMemberNotFound(exception: MemberNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(code = "MEMBER_NOT_FOUND", message = exception.message ?: "회원 정보를 찾을 수 없습니다."))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationFailed(exception: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val message = exception.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            ?: "입력한 회원 정보를 다시 확인해주세요."

        return ResponseEntity.badRequest()
            .body(ApiErrorResponse(code = "MEMBER_INVALID_REQUEST", message = message))
    }
}
