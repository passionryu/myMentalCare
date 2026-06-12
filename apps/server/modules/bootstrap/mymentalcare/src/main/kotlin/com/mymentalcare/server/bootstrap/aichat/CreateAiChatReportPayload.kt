package com.mymentalcare.server.bootstrap.aichat

import jakarta.validation.constraints.Size

data class CreateAiChatReportPayload(
    val forceCreate: Boolean = false,

    @field:Size(max = 80, message = "요청 식별자는 80자 이하로 입력해주세요.")
    val clientRequestId: String? = null,
)
