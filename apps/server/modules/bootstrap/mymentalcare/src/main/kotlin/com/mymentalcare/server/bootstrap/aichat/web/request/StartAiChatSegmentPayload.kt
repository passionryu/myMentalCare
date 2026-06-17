package com.mymentalcare.server.bootstrap.aichat.web.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class StartAiChatSegmentPayload(
    @field:NotBlank(message = "시작 방식을 선택해주세요.")
    val startType: String,

    @field:Size(max = 80, message = "요청 식별자는 80자 이하로 입력해주세요.")
    val clientRequestId: String? = null,
)
