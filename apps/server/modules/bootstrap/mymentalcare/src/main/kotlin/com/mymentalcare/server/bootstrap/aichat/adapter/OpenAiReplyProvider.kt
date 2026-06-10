package com.mymentalcare.server.bootstrap.aichat.adapter

import com.mymentalcare.server.application.aichat.AiReplyFailureNotification
import com.mymentalcare.server.application.aichat.AiReplyProvider
import com.mymentalcare.server.application.aichat.AiReplyRequest
import com.mymentalcare.server.application.aichat.AiReplyResponse
import com.mymentalcare.server.application.aichat.OPEN_AI_REPLY_ERROR_MESSAGE
import com.mymentalcare.server.application.common.extension.logError
import com.mymentalcare.server.application.common.extension.logWarn
import com.mymentalcare.server.application.port.OperatorNotificationPort
import com.mymentalcare.server.bootstrap.config.OpenAiProperties
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class OpenAiReplyProvider(
    private val openAiProperties: OpenAiProperties,
    private val openAiReplyClient: OpenAiReplyClient,
    private val operatorNotificationPort: OperatorNotificationPort,
    private val environment: Environment,
) : AiReplyProvider {
    // OpenAI 응답 생성 실패를 사용자 안내와 운영자 알림으로 안전하게 처리한다.
    override fun generateReply(request: AiReplyRequest): AiReplyResponse {
        if (openAiProperties.apiKey.isBlank()) {
            logWarn {
                "[AI 마음 대화] OpenAI API Key 미설정으로 응답 생성 실패. " +
                    "who=memberId:${request.memberId}, " +
                    "what=AiReplyProvider.generateReply, " +
                    "requestData=roomId:${request.roomId},messageId:${request.messageId}, " +
                    "reason=apiKeyMissing"
            }
            notifyFailure(request, OpenAiReplyFailureType.API_KEY_MISSING)
            return AiReplyResponse(
                content = OPEN_AI_REPLY_ERROR_MESSAGE,
                failed = true,
            )
        }

        return try {
            AiReplyResponse(openAiReplyClient.requestMindReply(request))
        } catch (e: OpenAiReplyGenerationFailedException) {
            logError(e) {
                "[AI 마음 대화] OpenAI 응답 생성 실패. " +
                    "who=memberId:${request.memberId}, " +
                    "what=AiReplyProvider.generateReply, " +
                    "requestData=roomId:${request.roomId},messageId:${request.messageId},recentMessageCount:${request.recentMessages.size}, " +
                    "reason=${e.failureType.name}"
            }
            notifyFailure(request, e.failureType)
            AiReplyResponse(
                content = OPEN_AI_REPLY_ERROR_MESSAGE,
                failed = true,
            )
        }
    }

    private fun notifyFailure(request: AiReplyRequest, failureType: OpenAiReplyFailureType) {
        operatorNotificationPort.notifyAiReplyFailure(
            AiReplyFailureNotification(
                occurredAt = LocalDateTime.now(),
                environment = environment.activeProfiles.firstOrNull() ?: "local",
                model = openAiProperties.model,
                memberId = request.memberId,
                roomId = request.roomId,
                messageId = request.messageId,
                failureType = failureType.name.lowercase(),
                fallbackUsed = true,
            )
        )
    }
}
