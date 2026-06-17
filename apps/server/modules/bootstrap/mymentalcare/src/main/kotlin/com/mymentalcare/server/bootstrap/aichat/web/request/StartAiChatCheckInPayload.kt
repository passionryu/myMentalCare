package com.mymentalcare.server.bootstrap.aichat.web.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class StartAiChatCheckInPayload(
    @field:NotBlank(message = "체크인 유형을 선택해주세요.")
    val templateType: String,

    @field:NotEmpty(message = "체크인 답변을 입력해주세요.")
    @field:Valid
    val answers: List<AiChatCheckInAnswerPayload>,

    @field:Size(max = 80, message = "요청 식별자는 80자 이하로 입력해주세요.")
    val clientRequestId: String? = null,
)

data class AiChatCheckInAnswerPayload(
    @field:NotBlank(message = "체크인 단계 정보가 필요합니다.")
    val stepKey: String,

    val optionKey: String? = null,

    val label: String? = null,

    val value: Int? = null,

    @field:Size(max = 200, message = "직접 입력은 200자 이하로 입력해주세요.")
    val freeText: String? = null,
)
