package com.mymentalcare.server.bootstrap.aichat.adapter.openai

import com.mymentalcare.server.application.aichat.response.AiReplyFailureNotification
import com.mymentalcare.server.application.aichat.request.AiReplyMessage
import com.mymentalcare.server.application.aichat.request.AiReplyRequest
import com.mymentalcare.server.application.aichat.policy.OPEN_AI_REPLY_ERROR_MESSAGE
import com.mymentalcare.server.application.notification.port.OperatorNotificationPort
import com.mymentalcare.server.bootstrap.config.OpenAiProperties
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.env.MockEnvironment

class OpenAiReplyProviderTest {
    @Test
    fun `OpenAI 응답 생성에 성공하면 마음이 응답을 반환한다`() {
        val provider = openAiReplyProvider(
            openAiReplyClient = FakeOpenAiReplyClient(reply = "오늘 마음이 많이 무거웠겠어요."),
        )

        val response = provider.generateReply(aiReplyRequest())

        assertEquals("오늘 마음이 많이 무거웠겠어요.", response.content)
        assertEquals(false, response.failed)
    }

    @Test
    fun `OpenAI API Key가 없으면 사용자 오류 안내를 반환하고 운영자 알림을 남긴다`() {
        val notificationPort = FakeOperatorNotificationPort()
        val provider = openAiReplyProvider(
            openAiProperties = OpenAiProperties(apiKey = ""),
            operatorNotificationPort = notificationPort,
        )

        val response = provider.generateReply(aiReplyRequest())

        assertEquals(OPEN_AI_REPLY_ERROR_MESSAGE, response.content)
        assertEquals(true, response.failed)
        assertEquals("api_key_missing", notificationPort.notifications.single().failureType)
        assertEquals(true, notificationPort.notifications.single().fallbackUsed)
    }

    @Test
    fun `OpenAI 호출이 실패하면 사용자 오류 안내를 반환하고 실패 유형을 알린다`() {
        val notificationPort = FakeOperatorNotificationPort()
        val provider = openAiReplyProvider(
            openAiReplyClient = FakeOpenAiReplyClient(failureType = OpenAiReplyFailureType.RATE_LIMIT),
            operatorNotificationPort = notificationPort,
        )

        val response = provider.generateReply(aiReplyRequest())

        assertEquals(OPEN_AI_REPLY_ERROR_MESSAGE, response.content)
        assertEquals(true, response.failed)
        assertEquals("rate_limit", notificationPort.notifications.single().failureType)
        assertEquals(1L, notificationPort.notifications.single().memberId)
        assertEquals(10L, notificationPort.notifications.single().roomId)
        assertEquals(20L, notificationPort.notifications.single().messageId)
        assertEquals(true, notificationPort.notifications.single().fallbackUsed)
    }

    private fun openAiReplyProvider(
        openAiProperties: OpenAiProperties = OpenAiProperties(apiKey = "test-api-key", model = "gpt-5-nano"),
        openAiReplyClient: OpenAiReplyClient = FakeOpenAiReplyClient(reply = "마음이 응답입니다."),
        operatorNotificationPort: FakeOperatorNotificationPort = FakeOperatorNotificationPort(),
    ): OpenAiReplyProvider {
        return OpenAiReplyProvider(
            openAiProperties = openAiProperties,
            openAiReplyClient = openAiReplyClient,
            operatorNotificationPort = operatorNotificationPort,
            environment = MockEnvironment().withProperty("spring.profiles.active", "test"),
        )
    }

    private fun aiReplyRequest(): AiReplyRequest {
        return AiReplyRequest(
            memberId = 1L,
            roomId = 10L,
            messageId = 20L,
            summaryContext = null,
            recentMessages = listOf(
                AiReplyMessage(
                    senderType = ChatMessageSenderType.USER,
                    content = "오늘 힘들었어",
                )
            ),
        )
    }

    private class FakeOpenAiReplyClient(
        private val reply: String = "",
        private val failureType: OpenAiReplyFailureType? = null,
    ) : OpenAiReplyClient {
        override fun requestMindReply(request: AiReplyRequest): String {
            failureType?.let {
                throw OpenAiReplyGenerationFailedException(it, "테스트 실패")
            }
            return reply
        }
    }

    private class FakeOperatorNotificationPort : OperatorNotificationPort {
        val notifications = mutableListOf<AiReplyFailureNotification>()

        override fun notifyAiReplyFailure(notification: AiReplyFailureNotification) {
            notifications.add(notification)
        }
    }
}
