package com.mymentalcare.server.application.aichat.policy

import com.mymentalcare.server.application.aichat.port.*
import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.ChatMessage
import org.springframework.stereotype.Component

private const val SUMMARY_REFRESH_MESSAGE_THRESHOLD = 6

@Component
internal class AiChatSummaryRefreshDecider {
    // 요약에 아직 반영되지 않은 메시지가 충분히 쌓였는지 판단한다.
    fun shouldRefreshTodaySummary(unsummarizedMessages: List<ChatMessage>): Boolean {
        return unsummarizedMessages.size >= SUMMARY_REFRESH_MESSAGE_THRESHOLD
    }
}
