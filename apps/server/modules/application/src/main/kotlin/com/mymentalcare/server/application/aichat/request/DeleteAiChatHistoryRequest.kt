package com.mymentalcare.server.application.aichat.request

import com.mymentalcare.server.application.aichat.response.*

data class DeleteAiChatHistoryRequest(
    val targetType: String,
    val targetId: Long,
)

data class DeleteAiChatHistoryResponse(
    val deletedCount: Int,
)
