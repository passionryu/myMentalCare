package com.mymentalcare.server.application.aichat

data class DeleteAiChatHistoryRequest(
    val targetType: String,
    val targetId: Long,
)

data class DeleteAiChatHistoryResponse(
    val deletedCount: Int,
)
