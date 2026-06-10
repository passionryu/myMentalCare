package com.mymentalcare.server.bootstrap.aichat.adapter

import com.mymentalcare.server.application.aichat.AiReplyMessage
import com.mymentalcare.server.application.aichat.AiReplyRequest
import com.mymentalcare.server.bootstrap.config.OpenAiProperties
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClient

class OpenAiChatClientTest {
    @Test
    fun `사용자 질문 우선 답변과 반복 방지 정책을 OpenAI 입력에 포함한다`() {
        val client = OpenAiChatClient(
            restClientBuilder = RestClient.builder(),
            openAiProperties = OpenAiProperties(apiKey = "test-api-key"),
        )

        val input = client.buildOpenAiInput(
            AiReplyRequest(
                memberId = 1L,
                roomId = 10L,
                messageId = 20L,
                summaryContext = null,
                recentMessages = listOf(
                    assistantMessage("오래된 답변에서는 잠깐 산책을 제안했어요."),
                    userMessage("오늘은 일이 많아서 지쳤어."),
                    assistantMessage("첫 번째 답변에서는 천천히 호흡하자고 했어요."),
                    userMessage("그래도 마음이 계속 불편해."),
                    assistantMessage("두 번째 답변에서는 어깨를 풀어보자고 했어요."),
                    assistantMessage("세 번째 답변에서는 물 한 잔을 마시자고 했어요."),
                    assistantMessage("네 번째 답변에서는 짧은 메모를 해보자고 했어요."),
                    userMessage("내가 지금 뭘 먼저 해야 할까?"),
                ),
            )
        )

        val systemMessages = input.filter { it["role"] == "system" }.map { it.getValue("content") }
        val basePolicy = systemMessages.first()
        val repetitionPolicy = systemMessages.single { it.contains("최근 마음이 답변 3개") }

        assertTrue(basePolicy.contains("사용자가 질문을 하면 질문에 먼저 답하고"))
        assertTrue(basePolicy.contains("행동 제안은 매번 하지 말고"))
        assertTrue(basePolicy.contains("앱의 고정 안전 안내 정책이 우선한다."))
        assertTrue(basePolicy.contains("명시적 조언 요청"))
        assertTrue(repetitionPolicy.contains("같은 행동 제안을 반복하지 않는다."))
        assertFalse(repetitionPolicy.contains("오래된 답변"))
        assertFalse(repetitionPolicy.contains("첫 번째 답변"))
        assertTrue(repetitionPolicy.contains("두 번째 답변"))
        assertTrue(repetitionPolicy.contains("세 번째 답변"))
        assertTrue(repetitionPolicy.contains("네 번째 답변"))
        assertEquals("내가 지금 뭘 먼저 해야 할까?", input.last().getValue("content"))
    }

    @Test
    fun `마음이 페르소나와 안전 응답 경계를 OpenAI 입력에 포함한다`() {
        val client = OpenAiChatClient(
            restClientBuilder = RestClient.builder(),
            openAiProperties = OpenAiProperties(apiKey = "test-api-key"),
        )

        val input = client.buildOpenAiInput(
            AiReplyRequest(
                memberId = 1L,
                roomId = 10L,
                messageId = 20L,
                summaryContext = null,
                recentMessages = listOf(userMessage("마음이가 어떤 역할을 해줄 수 있어?")),
            )
        )

        val basePolicy = input.first().getValue("content")

        assertTrue(basePolicy.contains("상담사, 의사, 진단자, 치료자가 아니라"))
        assertTrue(basePolicy.contains("의료 전문가나 상담 전문가처럼 소개하지 않는다."))
        assertTrue(basePolicy.contains("진단, 치료, 약물, 법률 조언과 확정적 판단은 하지 않는다."))
        assertTrue(basePolicy.contains("위기 표현이 감지된 상황에서는 일반 대화보다 앱의 고정 안전 안내 정책이 우선한다."))
    }

    @Test
    fun `일상 대화와 긍정 공유는 상담 질문으로 전환하지 않도록 안내한다`() {
        val client = OpenAiChatClient(
            restClientBuilder = RestClient.builder(),
            openAiProperties = OpenAiProperties(apiKey = "test-api-key"),
        )

        val input = client.buildOpenAiInput(
            AiReplyRequest(
                memberId = 1L,
                roomId = 10L,
                messageId = 20L,
                summaryContext = null,
                recentMessages = listOf(userMessage("해가 쨍쨍하고, 날씨도 좋잖아")),
            )
        )

        val basePolicy = input.first().getValue("content")

        assertTrue(basePolicy.contains("일상 이야기를 하면 자연스럽게 받아주고 상담 질문으로 급하게 전환하지 않는다."))
        assertTrue(basePolicy.contains("긍정적인 일을 공유하면 이유를 캐묻기보다 그 순간의 좋음을 함께 인정한다."))
        assertTrue(basePolicy.contains("사용자가 이미 이유를 설명했으면 같은 이유를 다시 묻지 않는다."))
        assertTrue(basePolicy.contains("사용자가 말하지 않은 분노, 답답함, 불안 같은 부정 감정을 추론하지 않는다."))
        assertTrue(basePolicy.contains("오늘 하루는 화창하네"))
        assertTrue(basePolicy.contains("창밖을 봐 화창하잖아"))
        assertTrue(basePolicy.contains("나쁜 답변 기준"))
    }

    // 사용자 메시지 테스트 데이터를 만든다.
    private fun userMessage(content: String): AiReplyMessage {
        return AiReplyMessage(
            senderType = ChatMessageSenderType.USER,
            content = content,
        )
    }

    // 마음이 응답 테스트 데이터를 만든다.
    private fun assistantMessage(content: String): AiReplyMessage {
        return AiReplyMessage(
            senderType = ChatMessageSenderType.ASSISTANT,
            content = content,
        )
    }
}
