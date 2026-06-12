package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.domain.aichat.AiChatReportSong
import com.mymentalcare.server.domain.aichat.AiChatReportType
import com.mymentalcare.server.domain.aichat.ChatMessage

interface AiChatReportGenerator {
    // 오늘 대화 메시지를 근거로 저장 가능한 마음 리포트 초안을 만든다.
    fun generateReport(reportType: AiChatReportType, messages: List<ChatMessage>): AiChatReportDraft
}

data class AiChatReportDraft(
    val summary: String,
    val primaryEmotion: String,
    val emotionIntensity: Int?,
    val mainCause: String,
    val emotionalFlow: String,
    val todaySentence: String,
    val songs: List<AiChatReportSong>,
)
