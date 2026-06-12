package com.mymentalcare.server.bootstrap.aichat

import com.mymentalcare.server.application.aichat.AiChatInvalidRequestException
import com.mymentalcare.server.bootstrap.auth.ApiErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AiChatExceptionHandler {
    @ExceptionHandler(AiChatInvalidRequestException::class)
    fun handleInvalidAiChatRequest(exception: AiChatInvalidRequestException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.badRequest()
            .body(ApiErrorResponse(code = "AI_CHAT_INVALID_REQUEST", message = exception.message ?: "마음 대화 요청을 다시 확인해주세요."))
    }
}
