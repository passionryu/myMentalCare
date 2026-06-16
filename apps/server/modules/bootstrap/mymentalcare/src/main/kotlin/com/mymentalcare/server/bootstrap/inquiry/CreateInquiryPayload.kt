package com.mymentalcare.server.bootstrap.inquiry

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateInquiryPayload(
    @field:NotBlank(message = "문의 유형을 선택해주세요.")
    @field:Size(max = 30, message = "문의 유형은 30자 이하로 입력해주세요.")
    val category: String,

    @field:NotBlank(message = "문의 내용을 입력해주세요.")
    @field:Size(min = 10, max = 2000, message = "문의 내용은 10자 이상 2000자 이하로 입력해주세요.")
    val content: String,
)
