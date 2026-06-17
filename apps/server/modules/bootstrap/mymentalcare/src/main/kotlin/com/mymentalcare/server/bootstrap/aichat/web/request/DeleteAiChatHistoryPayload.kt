package com.mymentalcare.server.bootstrap.aichat.web.request

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive

data class DeleteAiChatHistoryPayload(
    @field:Pattern(regexp = "CHAT_ROOM|REPORT|CHECK_IN", message = "삭제 대상을 확인해주세요.")
    val targetType: String,

    @field:Positive(message = "삭제 대상 ID를 확인해주세요.")
    val targetId: Long,
)

data class DeleteAiChatHistoryResponse(
    val deletedCount: Int,
)
