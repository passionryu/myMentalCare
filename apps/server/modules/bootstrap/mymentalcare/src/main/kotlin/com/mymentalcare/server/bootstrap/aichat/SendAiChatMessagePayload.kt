package com.mymentalcare.server.bootstrap.aichat

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SendAiChatMessagePayload(
    @field:NotBlank(message = "마음 대화 메시지를 입력해주세요.")
    @field:Size(max = 1000, message = "마음 대화 메시지는 1000자 이하로 입력해주세요.")
    val content: String,
)
