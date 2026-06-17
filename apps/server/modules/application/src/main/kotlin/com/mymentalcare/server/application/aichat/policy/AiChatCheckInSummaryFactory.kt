package com.mymentalcare.server.application.aichat.policy

import com.mymentalcare.server.application.aichat.port.*
import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.AiChatCheckInAnswer
import com.mymentalcare.server.domain.aichat.AiChatCheckInTemplateType
import org.springframework.stereotype.Component

private const val SUMMARY_TEXT_MAX_LENGTH = 120

@Component
internal class AiChatCheckInSummaryFactory {
    // 체크인 원본 답변을 대화 구간과 AI 컨텍스트에서 함께 쓸 짧은 문장으로 압축한다.
    fun buildSummaryText(templateType: AiChatCheckInTemplateType, answers: List<AiChatCheckInAnswer>): String {
        val summary = when (templateType) {
            AiChatCheckInTemplateType.BASIC_EMOTION -> buildBasicEmotionSummary(answers)
            AiChatCheckInTemplateType.CONVERSATION_START -> buildConversationStartSummary(answers)
            AiChatCheckInTemplateType.CONDITION -> buildConditionSummary(answers)
            AiChatCheckInTemplateType.DAY_REVIEW -> buildDayReviewSummary(answers)
        }

        return summary.take(SUMMARY_TEXT_MAX_LENGTH)
    }

    private fun buildBasicEmotionSummary(answers: List<AiChatCheckInAnswer>): String {
        val emotion = answers.readAnswerLabel("emotion") ?: "마음 상태"
        val intensity = answers.firstOrNull { it.stepKey == "intensity" }?.value?.let { "$it/5" }
        val reason = answers.readAnswerLabel("reason")

        return listOfNotNull(
            intensity?.let { "$emotion $it" } ?: emotion,
            reason,
        ).joinToString(" · ")
    }

    private fun buildConversationStartSummary(answers: List<AiChatCheckInAnswer>): String {
        val topic = answers.readAnswerLabel("topic") ?: "대화 시작"
        val responseStyle = answers.readAnswerLabel("responseStyle")

        return listOfNotNull(topic, responseStyle?.let { "$it 반응" }).joinToString(" · ")
    }

    private fun buildConditionSummary(answers: List<AiChatCheckInAnswer>): String {
        val energy = answers.readAnswerLabel("energy") ?: "컨디션"
        val factor = answers.readAnswerLabel("factor")

        return listOfNotNull(energy, factor).joinToString(" · ")
    }

    private fun buildDayReviewSummary(answers: List<AiChatCheckInAnswer>): String {
        val day = answers.readAnswerLabel("day") ?: "하루 회고"
        val emotion = answers.readAnswerLabel("remainingEmotion")
        val closing = answers.readAnswerLabel("closing")

        return listOfNotNull(day, emotion, closing).joinToString(" · ")
    }

    private fun List<AiChatCheckInAnswer>.readAnswerLabel(stepKey: String): String? {
        val answer = firstOrNull { it.stepKey == stepKey } ?: return null
        return answer.freeText?.trim()?.takeIf { it.isNotBlank() }
            ?: answer.label?.trim()?.takeIf { it.isNotBlank() }
    }
}
