package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import org.springframework.stereotype.Component

private const val MIN_USER_MESSAGE_COUNT = 10
private const val MIN_USER_TEXT_LENGTH = 80

@Component
internal class AiChatReportReadinessDecider {
    // 리포트에 필요한 최소 대화량만 판단하고, 감정은 리포트 생성 단계에서 조심스럽게 정리한다.
    fun decide(messages: List<ChatMessage>): AiChatReportReadinessResult {
        val userTexts = messages
            .filter { it.senderType == ChatMessageSenderType.USER }
            .map { it.content.trim() }
            .filter { it.isNotBlank() }
        val userTextLength = userTexts.sumOf { it.length }
        val unmetRequirements = buildList {
            if (userTexts.size < MIN_USER_MESSAGE_COUNT) {
                add(AiChatReportReadinessRequirement.USER_MESSAGE_COUNT.name)
            }
            if (userTextLength < MIN_USER_TEXT_LENGTH) {
                add(AiChatReportReadinessRequirement.USER_TEXT_LENGTH.name)
            }
        }
        val ready = unmetRequirements.isEmpty()

        return AiChatReportReadinessResult(
            ready = ready,
            reason = if (ready) "SUFFICIENT_CONVERSATION" else "SHORT_CONVERSATION",
            userMessageCount = userTexts.size,
            userTextLength = userTextLength,
            requiredUserMessageCount = MIN_USER_MESSAGE_COUNT,
            requiredUserTextLength = MIN_USER_TEXT_LENGTH,
            unmetRequirements = unmetRequirements,
            guideMessage = if (ready) null else SHORT_REPORT_GUIDE_MESSAGE,
        )
    }
}

data class AiChatReportReadinessResult(
    val ready: Boolean,
    val reason: String,
    val userMessageCount: Int,
    val userTextLength: Int,
    val requiredUserMessageCount: Int,
    val requiredUserTextLength: Int,
    val unmetRequirements: List<String>,
    val guideMessage: String?,
)

enum class AiChatReportReadinessRequirement {
    USER_MESSAGE_COUNT,
    USER_TEXT_LENGTH,
}

const val SHORT_REPORT_GUIDE_MESSAGE = "아직 제대로 된 마음 리포트를 작성하기에는 대화가 조금 더 필요합니다. 지금 리포트를 만들면, 확인된 내용만 바탕으로 간단히 정리됩니다."
