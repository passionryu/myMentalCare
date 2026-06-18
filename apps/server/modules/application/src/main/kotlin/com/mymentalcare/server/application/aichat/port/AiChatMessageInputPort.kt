package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.SendAiChatMessageRequest
import com.mymentalcare.server.application.aichat.request.StartAiChatSegmentRequest
import com.mymentalcare.server.application.aichat.response.SendAiChatMessageResponse
import com.mymentalcare.server.application.aichat.response.StartAiChatSegmentResponse

interface AiChatMessageInputPort {
    fun startSegment(memberId: Long, request: StartAiChatSegmentRequest): StartAiChatSegmentResponse

    fun sendMessage(memberId: Long, request: SendAiChatMessageRequest): SendAiChatMessageResponse
}
