package com.mymentalcare.server.bootstrap.aichat.adapter.openai

import com.mymentalcare.server.application.aichat.request.AiReplyRequest

interface OpenAiReplyClient {
    // OpenAI에 최근 대화 맥락을 전달하고 마음이의 응답 문장을 가져온다.
    fun requestMindReply(request: AiReplyRequest): String
}
