package com.mymentalcare.server.bootstrap.aichat.adapter.openai

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.mymentalcare.server.application.aichat.policy.DefaultAiChatReportGenerator
import com.mymentalcare.server.application.aichat.port.AiChatReportDraft
import com.mymentalcare.server.application.aichat.port.AiChatReportGenerator
import com.mymentalcare.server.application.common.extension.logWarn
import com.mymentalcare.server.bootstrap.config.OpenAiProperties
import com.mymentalcare.server.domain.aichat.AiChatReportEmotionPoint
import com.mymentalcare.server.domain.aichat.AiChatReportType
import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import kotlin.math.ceil

private const val REPORT_OPEN_AI_BASE_URL = "https://api.openai.com"
private const val REPORT_OPEN_AI_RESPONSES_PATH = "/v1/responses"
private const val REPORT_MAX_OUTPUT_TOKENS = 1_500
private const val REPORT_TEXT_MAX_LENGTH = 500
private const val REPORT_FIELD_MAX_LENGTH = 300
private const val REPORT_TIMELINE_MAX_COUNT = 8

@Component
class OpenAiChatReportGenerator(
    restClientBuilder: RestClient.Builder,
    private val openAiProperties: OpenAiProperties,
    private val objectMapper: ObjectMapper,
) : AiChatReportGenerator {
    private val fallbackGenerator = DefaultAiChatReportGenerator()
    private val restClient: RestClient = restClientBuilder
        .baseUrl(REPORT_OPEN_AI_BASE_URL)
        .requestFactory(
            JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                    .connectTimeout(openAiProperties.timeout)
                    .build()
            ).apply {
                setReadTimeout(openAiProperties.timeout)
            }
        )
        .build()

    // 오늘 대화 전체를 OpenAI가 읽고 대표 감정, 감정 점수, 변화 흐름을 구조화한다.
    override fun generateReport(reportType: AiChatReportType, messages: List<ChatMessage>): AiChatReportDraft {
        val fallbackDraft = fallbackGenerator.generateReport(reportType, messages)
        if (reportType == AiChatReportType.SHORT || openAiProperties.apiKey.isBlank()) {
            return fallbackDraft
        }

        return runCatching {
            val response = restClient.post()
                .uri(REPORT_OPEN_AI_RESPONSES_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${openAiProperties.apiKey}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(buildPayload(messages))
                .retrieve()
                .body(JsonNode::class.java)

            parseReportDraft(
                responseText = extractOutputText(response),
                fallbackDraft = fallbackDraft,
            )
        }.getOrElse { throwable ->
            logWarn {
                "[AI 마음 리포트] OpenAI 리포트 생성 실패로 fallback 사용. " +
                    "who=system, " +
                    "what=OpenAiChatReportGenerator.generateReport, " +
                    "requestData=messageCount:${messages.size}, " +
                    "reason=${throwable::class.simpleName}"
            }
            fallbackDraft
        }
    }

    private fun buildPayload(messages: List<ChatMessage>): Map<String, Any> {
        return mapOf(
            "model" to openAiProperties.model,
            "reasoning" to mapOf("effort" to "minimal"),
            "text" to mapOf("verbosity" to "low"),
            "max_output_tokens" to REPORT_MAX_OUTPUT_TOKENS,
            "input" to listOf(
                mapOf(
                    "role" to "system",
                    "content" to buildSystemPrompt(),
                ),
                mapOf(
                    "role" to "user",
                    "content" to buildTranscript(messages),
                ),
            ),
        )
    }

    private fun buildSystemPrompt(): String {
        return """
            당신은 Haru Mind의 마음 리포트 생성기입니다.
            전체 대화를 읽고 사용자의 마음을 진단이 아닌 회고/정리 관점으로만 요약하세요.
            반드시 JSON 객체만 반환하세요. Markdown, 설명 문장, 코드블록은 금지합니다.

            JSON 스키마:
            {
              "summary": "오늘 대화 요약 1~2문장",
              "primaryEmotion": "대표 감정 한국어 짧은 명사",
              "emotionScore": 0,
              "mainCause": "주요 원인 짧은 문구",
              "emotionalFlow": "감정 변화 그래프를 설명하는 1~3문장",
              "todaySentence": "오늘 마음을 정리하는 한 문장",
              "emotionTimeline": [
                {
                  "pointOrder": 1,
                  "messageOrder": 1,
                  "label": "초반",
                  "score": 0,
                  "reason": "해당 점수의 짧은 근거"
                }
              ]
            }

            판단 기준:
            - emotionScore는 0~100 정수입니다. 0은 매우 무겁고 부정적인 상태, 100은 안정적이고 긍정적인 상태입니다.
            - 사용자가 직접 말한 내용과 대화 흐름만 근거로 삼고, 말하지 않은 감정/원인을 단정하지 마세요.
            - emotionTimeline은 대화 타임라인을 따라 4~8개 포인트로 구성하세요.
            - messageOrder는 해당 포인트 판단에 가장 가까운 대화의 messageOrder를 사용하세요.
            - 의료 진단, 치료 판단, 위기 대응 역할을 하지 마세요.
        """.trimIndent()
    }

    private fun buildTranscript(messages: List<ChatMessage>): String {
        return messages
            .filter { it.senderType == ChatMessageSenderType.USER || it.senderType == ChatMessageSenderType.ASSISTANT }
            .sortedBy { it.messageOrder }
            .joinToString("\n") { message ->
                val role = if (message.senderType == ChatMessageSenderType.USER) "USER" else "ASSISTANT"
                "[${message.messageOrder}] $role: ${message.content.trim().take(REPORT_TEXT_MAX_LENGTH)}"
            }
            .ifBlank { "대화 내용 없음" }
    }

    private fun parseReportDraft(responseText: String, fallbackDraft: AiChatReportDraft): AiChatReportDraft {
        val json = objectMapper.readTree(responseText.extractJsonObject())
        val emotionScore = json.path("emotionScore").asInt(fallbackDraft.emotionScore ?: 50).coerceIn(0, 100)
        val timeline = parseTimeline(json.path("emotionTimeline"), fallbackDraft.emotionTimeline)

        return AiChatReportDraft(
            summary = json.textOrFallback("summary", fallbackDraft.summary),
            primaryEmotion = json.textOrFallback("primaryEmotion", fallbackDraft.primaryEmotion),
            emotionIntensity = emotionScore.toLegacyIntensity(),
            emotionScore = emotionScore,
            mainCause = json.textOrFallback("mainCause", fallbackDraft.mainCause),
            emotionalFlow = json.textOrFallback("emotionalFlow", fallbackDraft.emotionalFlow),
            todaySentence = json.textOrFallback("todaySentence", fallbackDraft.todaySentence),
            songs = fallbackDraft.songs,
            emotionTimeline = timeline.ifEmpty { fallbackDraft.emotionTimeline },
        )
    }

    private fun parseTimeline(timelineNode: JsonNode, fallbackTimeline: List<AiChatReportEmotionPoint>): List<AiChatReportEmotionPoint> {
        if (!timelineNode.isArray) {
            return fallbackTimeline
        }

        return timelineNode
            .take(REPORT_TIMELINE_MAX_COUNT)
            .mapIndexed { index, node ->
                AiChatReportEmotionPoint(
                    pointOrder = node.path("pointOrder").asInt(index + 1).coerceAtLeast(1),
                    messageOrder = node.path("messageOrder").takeIf { it.isInt || it.isTextual }?.asInt(),
                    label = node.textOrFallback("label", "구간 ${index + 1}", 80),
                    score = node.path("score").asInt(50).coerceIn(0, 100),
                    reason = node.textOrFallback("reason", "대화 흐름 기준", 240),
                )
            }
            .sortedBy { it.pointOrder }
    }

    private fun extractOutputText(response: JsonNode?): String {
        val outputText = response?.path("output_text")?.asText(null)
        if (!outputText.isNullOrBlank()) {
            return outputText.trim()
        }

        val text = response
            ?.path("output")
            ?.firstNotNullOfOrNull { output ->
                findOutputText(output.path("content"))
            }

        require(!text.isNullOrBlank()) { "OpenAI 리포트 응답 본문에서 JSON 텍스트를 찾지 못했습니다." }
        return text.trim()
    }

    private fun findOutputText(content: JsonNode?): String? {
        if (content == null || content.isMissingNode || content.isNull) {
            return null
        }

        if (content.isArray) {
            return content.firstNotNullOfOrNull { findOutputText(it) }
        }

        if (content.path("type").asText(null) == "output_text") {
            return content.path("text").asText(null)?.takeIf { it.isNotBlank() }
        }

        return content.path("text").asText(null)?.takeIf { it.isNotBlank() }
    }
}

private fun JsonNode.textOrFallback(fieldName: String, fallback: String, maxLength: Int = REPORT_FIELD_MAX_LENGTH): String {
    return path(fieldName).asText(null)?.trim()?.takeIf { it.isNotBlank() }?.take(maxLength) ?: fallback
}

private fun Int.toLegacyIntensity(): Int {
    return ceil(coerceIn(0, 100).toDouble() / 20.0).toInt().coerceIn(1, 5)
}

private fun String.extractJsonObject(): String {
    val trimmed = trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()
    val startIndex = trimmed.indexOf('{')
    val endIndex = trimmed.lastIndexOf('}')

    require(startIndex >= 0 && endIndex > startIndex) { "OpenAI 리포트 응답이 JSON 객체가 아닙니다." }
    return trimmed.substring(startIndex, endIndex + 1)
}
