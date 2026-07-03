package com.mymentalcare.server.application.aichat.policy

import com.mymentalcare.server.application.aichat.port.*
import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.AiChatReportEmotionPoint
import com.mymentalcare.server.domain.aichat.AiChatReportType
import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import kotlin.math.roundToInt

class DefaultAiChatReportGenerator : AiChatReportGenerator {
    // 대화가 충분하면 감정과 원인을 조심스럽게 정리하고, 부족하면 판단 유보 리포트를 만든다.
    override fun generateReport(reportType: AiChatReportType, messages: List<ChatMessage>): AiChatReportDraft {
        val userMessages = messages
            .filter { it.senderType == ChatMessageSenderType.USER && it.content.isNotBlank() }
            .sortedBy { it.messageOrder }
        val userTexts = userMessages.map { it.content.trim() }

        if (reportType == AiChatReportType.SHORT) {
            return buildShortReport()
        }

        val primaryEmotion = detectPrimaryEmotion(userTexts)
        val emotionScore = primaryEmotion.intensity.toEmotionScore()
        val mainCause = detectMainCause(userTexts)
        val recentTopics = userTexts.takeLast(3).joinToString(" / ") { it.take(70) }

        return AiChatReportDraft(
            summary = "오늘은 ${mainCause}와 관련된 이야기가 주로 오갔습니다. 최근 대화에서는 $recentTopics 같은 흐름이 확인되었습니다.",
            primaryEmotion = primaryEmotion.label,
            emotionIntensity = primaryEmotion.intensity,
            emotionScore = emotionScore,
            mainCause = mainCause,
            emotionalFlow = "대화 초반의 표현과 최근 메시지를 함께 보면, 마음을 바로 결론내리기보다 오늘의 상태를 정리하려는 흐름이 나타났습니다.",
            todaySentence = "오늘의 마음은 단정하기보다, 말로 꺼낸 만큼만 천천히 정리해도 괜찮습니다.",
            songs = emptyList(),
            emotionTimeline = buildFallbackTimeline(userMessages),
        )
    }

    private fun buildShortReport(): AiChatReportDraft {
        return AiChatReportDraft(
            summary = "오늘은 짧게 대화를 시작했지만, 마음 상태나 상황을 파악할 만큼의 내용은 충분히 쌓이지 않았습니다.",
            primaryEmotion = "아직 판단하기 어려움",
            emotionIntensity = null,
            emotionScore = null,
            mainCause = "확인되지 않음",
            emotionalFlow = "대화량이 적어 마음의 변화 흐름을 정리하기 어렵습니다.",
            todaySentence = "아직 더 말해도 괜찮습니다.",
            songs = emptyList(),
            emotionTimeline = emptyList(),
        )
    }

    private fun buildFallbackTimeline(userMessages: List<ChatMessage>): List<AiChatReportEmotionPoint> {
        if (userMessages.isEmpty()) {
            return emptyList()
        }

        val sampledMessages = when {
            userMessages.size <= 5 -> userMessages
            else -> listOf(
                userMessages.first(),
                userMessages[userMessages.lastIndex / 4],
                userMessages[userMessages.lastIndex / 2],
                userMessages[(userMessages.lastIndex * 3) / 4],
                userMessages.last(),
            ).distinctBy { it.messageOrder }
        }

        return sampledMessages.mapIndexed { index, message ->
            val emotion = detectPrimaryEmotion(listOf(message.content))
            AiChatReportEmotionPoint(
                pointOrder = index + 1,
                messageOrder = message.messageOrder,
                label = when (index) {
                    0 -> "초반"
                    sampledMessages.lastIndex -> "마무리"
                    else -> "중간 ${index}"
                },
                score = emotion.intensity.toEmotionScore(),
                reason = message.content.trim().take(60).ifBlank { "대화 흐름 기준" },
            )
        }
    }

    private fun detectPrimaryEmotion(userTexts: List<String>): EmotionGuess {
        val joinedText = userTexts.joinToString(" ")
        return when {
            listOf("불안", "걱정", "초조").any { joinedText.contains(it) } -> EmotionGuess("불안", 4)
            listOf("지침", "피곤", "기운", "번아웃").any { joinedText.contains(it) } -> EmotionGuess("지침", 4)
            listOf("우울", "슬퍼", "외로").any { joinedText.contains(it) } -> EmotionGuess("가라앉음", 4)
            listOf("화", "짜증", "분노", "답답").any { joinedText.contains(it) } -> EmotionGuess("답답함", 4)
            listOf("괜찮", "좋아", "편안", "안도").any { joinedText.contains(it) } -> EmotionGuess("안도", 3)
            else -> EmotionGuess("정리 중인 마음", 3)
        }
    }

    private fun detectMainCause(userTexts: List<String>): String {
        val joinedText = userTexts.joinToString(" ")
        return when {
            listOf("회사", "업무", "일", "프로젝트").any { joinedText.contains(it) } -> "업무 부담"
            listOf("공부", "학교", "시험", "과제").any { joinedText.contains(it) } -> "학업 부담"
            listOf("사람", "친구", "관계", "동료").any { joinedText.contains(it) } -> "인간관계"
            listOf("가족", "부모", "형제").any { joinedText.contains(it) } -> "가족"
            listOf("건강", "아파", "몸").any { joinedText.contains(it) } -> "건강"
            listOf("돈", "월급", "비용").any { joinedText.contains(it) } -> "돈"
            listOf("잠", "수면", "피곤").any { joinedText.contains(it) } -> "회복 부족"
            else -> "대화에서 드러난 일상 흐름"
        }
    }

}

private data class EmotionGuess(
    val label: String,
    val intensity: Int,
)

private fun Int.toEmotionScore(): Int {
    return ((coerceIn(0, 5).toDouble() / 5.0) * 100).roundToInt().coerceIn(0, 100)
}
