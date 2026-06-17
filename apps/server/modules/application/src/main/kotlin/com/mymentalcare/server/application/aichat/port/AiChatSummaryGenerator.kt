package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.AiChatRoomSummary
import com.mymentalcare.server.domain.aichat.ChatMessage

interface AiChatSummaryGenerator {
    // 기존 요약과 새 메시지를 합쳐 오늘 대화의 압축 메모리를 만든다.
    fun generateTodaySummary(
        existingSummary: AiChatRoomSummary?,
        unsummarizedMessages: List<ChatMessage>,
    ): AiChatSummaryGenerationResult
}
