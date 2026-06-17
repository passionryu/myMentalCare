package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

interface AiReplyProvider {
    // 사용자의 최근 대화 맥락을 기반으로 마음이의 답변을 생성한다.
    fun generateReply(request: AiReplyRequest): AiReplyResponse
}
