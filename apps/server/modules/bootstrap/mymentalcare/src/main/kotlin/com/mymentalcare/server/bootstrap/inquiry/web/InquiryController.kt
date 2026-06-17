package com.mymentalcare.server.bootstrap.inquiry.web

import com.mymentalcare.server.bootstrap.inquiry.web.request.*
import com.mymentalcare.server.bootstrap.inquiry.web.response.*

import com.mymentalcare.server.application.inquiry.request.CreateInquiryRequest
import com.mymentalcare.server.application.inquiry.port.InquiryInputPort
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/inquiries")
class InquiryController(
    private val inquiryInputPort: InquiryInputPort,
) {
    @Operation(
        summary = "문의 접수",
        description = "로그인한 사용자의 서비스 문의를 접수합니다.",
    )
    @PostMapping
    fun createInquiry(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody payload: CreateInquiryPayload,
    ): ResponseEntity<CreateInquiryResponse> {
        val response = inquiryInputPort.createInquiry(
            memberId = memberId,
            request = CreateInquiryRequest(
                category = payload.category,
                content = payload.content,
            ),
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                CreateInquiryResponse(
                    inquiryId = response.inquiryId,
                    createdAt = response.createdAt,
                    status = response.status,
                )
            )
    }
}
