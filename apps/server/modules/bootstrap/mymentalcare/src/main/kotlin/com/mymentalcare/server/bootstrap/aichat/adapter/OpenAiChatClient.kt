package com.mymentalcare.server.bootstrap.aichat.adapter

import com.fasterxml.jackson.databind.JsonNode
import com.mymentalcare.server.application.aichat.AiReplyMessage
import com.mymentalcare.server.application.aichat.AiReplyRequest
import com.mymentalcare.server.application.common.extension.logError
import com.mymentalcare.server.bootstrap.config.OpenAiProperties
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.net.SocketTimeoutException
import java.net.http.HttpClient

private const val OPEN_AI_BASE_URL = "https://api.openai.com"
private const val OPEN_AI_RESPONSES_PATH = "/v1/responses"
private const val MAX_OUTPUT_TOKENS = 900

@Component
class OpenAiChatClient(
    restClientBuilder: RestClient.Builder,
    private val openAiProperties: OpenAiProperties,
) : OpenAiReplyClient {
    private val restClient: RestClient = restClientBuilder
        .baseUrl(OPEN_AI_BASE_URL)
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

    // OpenAI Responses API를 호출하여 기본 챗봇 마음이의 짧은 공감형 답변을 생성한다.
    override fun requestMindReply(request: AiReplyRequest): String {
        val payload = mapOf(
            "model" to openAiProperties.model,
            "reasoning" to mapOf("effort" to "minimal"),
            "text" to mapOf("verbosity" to "low"),
            "max_output_tokens" to MAX_OUTPUT_TOKENS,
            "input" to buildOpenAiInput(request),
        )

        try {
            val response = restClient.post()
                .uri(OPEN_AI_RESPONSES_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${openAiProperties.apiKey}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(JsonNode::class.java)

            return MindChatResponsePolicy.polishGeneratedReply(
                reply = extractReplyText(response),
                latestUserMessage = request.recentMessages.lastOrNull()?.content.orEmpty(),
            )
        } catch (e: ResourceAccessException) {
            throw OpenAiReplyGenerationFailedException(OpenAiReplyFailureType.TIMEOUT, "OpenAI 응답 생성 요청 시간이 초과되었습니다.", e)
        } catch (e: HttpStatusCodeException) {
            throw OpenAiReplyGenerationFailedException(classifyHttpFailure(e), "OpenAI 응답 생성 HTTP 오류가 발생했습니다.", e)
        } catch (e: RestClientException) {
            throw OpenAiReplyGenerationFailedException(classifyRestClientFailure(e), "OpenAI 응답 생성 요청이 실패했습니다.", e)
        }
    }

    // 마음이의 응답 정책과 대화 맥락을 OpenAI 입력 메시지로 구성한다.
    internal fun buildOpenAiInput(request: AiReplyRequest): List<Map<String, String>> {
        val systemMessage = mapOf(
            "role" to "system",
            "content" to MindChatResponsePolicy.systemPrompt,
        )
        val summaryMemory = request.summaryContext?.let {
            mapOf(
                "role" to "system",
                "content" to buildSummaryMemoryContent(it),
            )
        }
        val repetitionGuard = buildRepetitionGuardContent(request.recentMessages)?.let {
            mapOf(
                "role" to "system",
                "content" to it,
            )
        }
        val recentMessages = request.recentMessages.map {
            mapOf(
                "role" to if (it.senderType == ChatMessageSenderType.ASSISTANT) "assistant" else "user",
                "content" to it.content,
            )
        }

        return listOfNotNull(systemMessage, summaryMemory, repetitionGuard) + recentMessages
    }

    // 오늘 대화 요약 메모리를 OpenAI가 참고할 수 있는 짧은 시스템 컨텍스트로 변환한다.
    private fun buildSummaryMemoryContent(summaryContext: com.mymentalcare.server.application.aichat.AiChatSummaryContext): String {
        return listOfNotNull(
            "오늘 대화 요약: ${summaryContext.summary}",
            summaryContext.emotionalState?.let { "현재 감정 상태: $it" },
            summaryContext.activeTopics?.let { "이어지는 주제: $it" },
            summaryContext.unresolvedQuestions?.let { "아직 열린 질문: $it" },
            summaryContext.userPreferences?.let { "사용자 선호: $it" },
        ).joinToString("\n")
    }

    // 최근 마음이 답변을 기준으로 같은 행동 제안이 반복되지 않게 안내한다.
    private fun buildRepetitionGuardContent(recentMessages: List<AiReplyMessage>): String? {
        val recentAssistantReplies = recentMessages
            .filter { it.senderType == ChatMessageSenderType.ASSISTANT }
            .takeLast(RECENT_ASSISTANT_REPLY_LIMIT)
            .mapIndexed { index, message -> "${index + 1}. ${message.content}" }

        if (recentAssistantReplies.isEmpty()) {
            return null
        }

        return listOf(
            "최근 마음이 답변 ${recentAssistantReplies.size}개를 참고해 같은 행동 제안을 반복하지 않는다.",
            "이미 비슷한 질문이나 행동 제안을 했다면 이번 답변은 질문 없이 짧은 반응이나 맞장구로 끝낸다.",
            "사용자가 직접 질문한 경우에는 질문에 대한 답변을 우선한다.",
            "최근 마음이 답변:",
            *recentAssistantReplies.toTypedArray(),
        ).joinToString("\n")
    }

    private fun extractReplyText(response: JsonNode?): String {
        val outputText = response?.path("output_text")?.asText(null)
        if (!outputText.isNullOrBlank()) {
            return outputText.trim()
        }

        val text = response
            ?.path("output")
            ?.firstNotNullOfOrNull { output ->
                findOutputText(output.path("content"))
            }

        if (!text.isNullOrBlank()) {
            return text.trim()
        }

        logError(
            "[AI 마음 대화] OpenAI 응답 파싱 실패. " +
                "who=system, " +
                "what=POST /v1/responses, " +
                "requestData=omitted, " +
                "reason=responseTextMissing"
        )
        throw OpenAiReplyGenerationFailedException(OpenAiReplyFailureType.UNKNOWN, "OpenAI 응답 본문에서 답변을 찾지 못했습니다.")
    }

    // OpenAI output content 목록에서 실제 사용자에게 보여줄 답변 텍스트를 찾는다.
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

    // OpenAI HTTP 상태 코드를 운영자가 이해할 수 있는 실패 유형으로 분류한다.
    private fun classifyHttpFailure(e: HttpStatusCodeException): OpenAiReplyFailureType {
        return when (e.statusCode.value()) {
            401, 403 -> OpenAiReplyFailureType.UNAUTHORIZED
            429 -> OpenAiReplyFailureType.RATE_LIMIT
            in 500..599 -> OpenAiReplyFailureType.SERVER_ERROR
            else -> OpenAiReplyFailureType.UNKNOWN
        }
    }

    // RestClient 계층의 실패 원인 중 timeout 여부를 구분한다.
    private fun classifyRestClientFailure(e: RestClientException): OpenAiReplyFailureType {
        return if (e.containsCause<SocketTimeoutException>()) {
            OpenAiReplyFailureType.TIMEOUT
        } else {
            OpenAiReplyFailureType.UNKNOWN
        }
    }

    // 예외 체인을 따라가며 특정 원인 예외가 포함되어 있는지 확인한다.
    private inline fun <reified T : Throwable> Throwable.containsCause(): Boolean {
        var current: Throwable? = this
        while (current != null) {
            if (current is T) {
                return true
            }
            current = current.cause
        }
        return false
    }
}
