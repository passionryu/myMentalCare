package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.StartAiChatCheckInRequest
import com.mymentalcare.server.application.aichat.response.AiChatCheckInHistoryResponse
import com.mymentalcare.server.application.aichat.response.StartAiChatSegmentResponse

interface AiChatCheckInInputPort {
    fun readCheckIns(memberId: Long): List<AiChatCheckInHistoryResponse>

    fun startCheckInSegment(memberId: Long, request: StartAiChatCheckInRequest): StartAiChatSegmentResponse
}
