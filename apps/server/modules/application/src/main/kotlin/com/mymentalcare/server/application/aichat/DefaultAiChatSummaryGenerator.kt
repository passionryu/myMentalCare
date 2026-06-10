package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.domain.aichat.AiChatRoomSummary
import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import org.springframework.stereotype.Component

private const val SUMMARY_MESSAGE_PREVIEW_LIMIT = 4
private const val SUMMARY_MESSAGE_MAX_LENGTH = 80

@Component
internal class DefaultAiChatSummaryGenerator : AiChatSummaryGenerator {
    // 오늘 대화의 핵심 감정과 주제를 비용 없이 압축해 응답 컨텍스트로 사용할 요약을 만든다.
    override fun generateTodaySummary(
        existingSummary: AiChatRoomSummary?,
        unsummarizedMessages: List<ChatMessage>,
    ): AiChatSummaryGenerationResult {
        val userMessages = unsummarizedMessages
            .filter { it.senderType == ChatMessageSenderType.USER }
            .map { it.content.trim() }
            .filter { it.isNotBlank() }

        val recentTopicText = userMessages
            .takeLast(SUMMARY_MESSAGE_PREVIEW_LIMIT)
            .joinToString(" / ") { it.take(SUMMARY_MESSAGE_MAX_LENGTH) }
            .ifBlank { "사용자는 오늘 마음 상태를 천천히 공유하고 있다." }

        val previousSummary = existingSummary?.summary
            ?.takeIf { it.isNotBlank() }
            ?.let { "이전 요약: $it " }
            ?: ""

        return AiChatSummaryGenerationResult(
            summary = "${previousSummary}최근 대화: $recentTopicText".trim(),
            emotionalState = detectEmotionalState(userMessages) ?: existingSummary?.emotionalState,
            activeTopics = recentTopicText,
            unresolvedQuestions = readUnresolvedQuestions(userMessages) ?: existingSummary?.unresolvedQuestions,
            userPreferences = existingSummary?.userPreferences,
        )
    }

    private fun detectEmotionalState(userMessages: List<String>): String? {
        val joined = userMessages.joinToString(" ")
        return when {
            listOf("불안", "걱정", "초조").any { joined.contains(it) } -> "불안과 걱정"
            listOf("화", "짜증", "분노").any { joined.contains(it) } -> "분노와 답답함"
            listOf("슬퍼", "우울", "외로").any { joined.contains(it) } -> "슬픔과 외로움"
            listOf("괜찮", "편안", "나아").any { joined.contains(it) } -> "안도감"
            else -> null
        }
    }

    private fun readUnresolvedQuestions(userMessages: List<String>): String? {
        return userMessages
            .filter { it.contains("?") || it.contains("어떻게") || it.contains("뭘") }
            .takeLast(2)
            .joinToString(" / ") { it.take(SUMMARY_MESSAGE_MAX_LENGTH) }
            .ifBlank { null }
    }
}
