package com.mymentalcare.server.bootstrap.aichat.adapter

import com.fasterxml.jackson.databind.JsonNode
import com.mymentalcare.server.application.aichat.AiReplyRequest
import com.mymentalcare.server.application.common.extension.logError
import com.mymentalcare.server.bootstrap.config.OpenAiProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

private const val OPEN_AI_BASE_URL = "https://api.openai.com"
private const val OPEN_AI_RESPONSES_PATH = "/v1/responses"

@Component
class OpenAiChatClient(
    restClientBuilder: RestClient.Builder,
    private val openAiProperties: OpenAiProperties,
) : OpenAiReplyClient {
    private val restClient: RestClient = restClientBuilder
        .baseUrl(OPEN_AI_BASE_URL)
        .requestFactory(
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(openAiProperties.timeout)
                setReadTimeout(openAiProperties.timeout)
            }
        )
        .build()

    // OpenAI Responses API를 호출하여 기본 챗봇 마음이의 짧은 공감형 답변을 생성한다.
    override fun requestMindReply(request: AiReplyRequest): String {
        val payload = mapOf(
            "model" to openAiProperties.model,
            "input" to buildOpenAiInput(request),
        )

        try {
            val response = restClient.post()
                .uri(OPEN_AI_RESPONSES_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${openAiProperties.apiKey}")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(JsonNode::class.java)

            return extractReplyText(response)
        } catch (e: ResourceAccessException) {
            throw OpenAiReplyGenerationFailedException(OpenAiReplyFailureType.TIMEOUT, "OpenAI 응답 생성 요청 시간이 초과되었습니다.", e)
        } catch (e: HttpStatusCodeException) {
            throw OpenAiReplyGenerationFailedException(classifyHttpFailure(e), "OpenAI 응답 생성 HTTP 오류가 발생했습니다.", e)
        } catch (e: RestClientException) {
            throw OpenAiReplyGenerationFailedException(OpenAiReplyFailureType.UNKNOWN, "OpenAI 응답 생성 요청이 실패했습니다.", e)
        }
    }

    private fun buildOpenAiInput(request: AiReplyRequest): List<Map<String, String>> {
        val systemMessage = mapOf(
            "role" to "system",
            "content" to "너는 myMentalCare의 기본 챗봇 \"마음이\"다. 상담사/의사처럼 진단하지 말고, 감정을 정리하도록 돕는 따뜻한 대화 파트너로 답한다. 한국어로 2~4문장만 답하고, 먼저 감정을 인정한 뒤 작은 다음 행동이나 부담 없는 질문을 제안한다. 치료, 약물, 법률, 확정적 판단은 하지 않는다. 위기 상황은 앱의 고정 안전 안내 정책이 우선한다.",
        )
        val recentMessages = request.recentMessages.map {
            mapOf(
                "role" to if (it.senderType.name == "ASSISTANT") "assistant" else "user",
                "content" to it.content,
            )
        }

        return listOf(systemMessage) + recentMessages
    }

    private fun extractReplyText(response: JsonNode?): String {
        val outputText = response?.path("output_text")?.asText(null)
        if (!outputText.isNullOrBlank()) {
            return outputText.trim()
        }

        val text = response
            ?.path("output")
            ?.firstOrNull()
            ?.path("content")
            ?.firstOrNull()
            ?.path("text")
            ?.asText(null)

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

    private fun classifyHttpFailure(e: HttpStatusCodeException): OpenAiReplyFailureType {
        return when (e.statusCode.value()) {
            401, 403 -> OpenAiReplyFailureType.UNAUTHORIZED
            429 -> OpenAiReplyFailureType.RATE_LIMIT
            in 500..599 -> OpenAiReplyFailureType.SERVER_ERROR
            else -> OpenAiReplyFailureType.UNKNOWN
        }
    }
}
