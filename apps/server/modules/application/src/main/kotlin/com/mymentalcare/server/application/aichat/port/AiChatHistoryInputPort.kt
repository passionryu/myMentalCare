package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.DeleteAiChatHistoryRequest
import com.mymentalcare.server.application.aichat.request.DeleteAiChatHistoryResponse

interface AiChatHistoryInputPort {
    fun deleteHistory(memberId: Long, request: DeleteAiChatHistoryRequest): DeleteAiChatHistoryResponse
}
