package com.mymentalcare.server.bootstrap.notification.adapter.discord

import com.mymentalcare.server.application.aichat.response.AiReplyFailureNotification
import com.mymentalcare.server.application.common.extension.logError
import com.mymentalcare.server.application.common.extension.logWarn
import com.mymentalcare.server.application.notification.port.OperatorNotificationPort
import com.mymentalcare.server.application.notification.response.InquiryReceivedNotification
import com.mymentalcare.server.bootstrap.config.OperatorNotificationProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.time.format.DateTimeFormatter

private const val DISCORD_INQUIRY_CONTENT_LIMIT = 1_500

@Component
class DiscordOperatorNotificationAdapter(
    restClientBuilder: RestClient.Builder,
    private val properties: OperatorNotificationProperties,
) : OperatorNotificationPort {
    private val restClient = restClientBuilder.build()

    // AI 응답 생성 실패를 Discord 웹훅으로 운영자에게 알린다.
    override fun notifyAiReplyFailure(notification: AiReplyFailureNotification) {
        if (properties.discordWebhookUrl.isBlank()) {
            logWarn {
                "[AI 마음 대화] Discord 웹훅 미설정으로 운영자 알림 생략. " +
                    "who=system, " +
                    "what=DiscordOperatorNotificationAdapter.notifyAiReplyFailure, " +
                    "requestData=memberId:${notification.memberId},roomId:${notification.roomId},messageId:${notification.messageId}, " +
                    "reason=discordWebhookUrlMissing"
            }
            return
        }

        try {
            restClient.post()
                .uri(properties.discordWebhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("content" to buildOpenAiFailureMessage(notification)))
                .retrieve()
                .toBodilessEntity()
        } catch (e: RestClientException) {
            logError(e) {
                "[AI 마음 대화] Discord 운영자 알림 전송 실패. " +
                    "who=system, " +
                    "what=DiscordOperatorNotificationAdapter.notifyAiReplyFailure, " +
                    "requestData=memberId:${notification.memberId},roomId:${notification.roomId},messageId:${notification.messageId}, " +
                    "reason=${e.message}"
            }
        }
    }

    // 문의 접수 사실을 현재 운영 오류 Discord 채널로 임시 라우팅한다.
    override fun notifyInquiryReceived(notification: InquiryReceivedNotification) {
        if (properties.discordWebhookUrl.isBlank()) {
            logWarn {
                "[문의 접수] Discord 웹훅 미설정으로 운영자 문의 알림 생략. " +
                    "who=system, " +
                    "what=DiscordOperatorNotificationAdapter.notifyInquiryReceived, " +
                    "requestData=inquiryId:${notification.inquiryId},memberId:${notification.memberId},category:${notification.category}, " +
                    "reason=discordWebhookUrlMissing"
            }
            return
        }

        try {
            restClient.post()
                .uri(properties.discordWebhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("content" to buildInquiryReceivedMessage(notification)))
                .retrieve()
                .toBodilessEntity()
        } catch (e: RestClientException) {
            logError(e) {
                "[문의 접수] Discord 운영자 문의 알림 전송 실패. " +
                    "who=system, " +
                    "what=DiscordOperatorNotificationAdapter.notifyInquiryReceived, " +
                    "requestData=inquiryId:${notification.inquiryId},memberId:${notification.memberId},category:${notification.category}, " +
                    "reason=${e.message}"
            }
        }
    }

    private fun buildOpenAiFailureMessage(notification: AiReplyFailureNotification): String {
        return """
            [myMentalCare OpenAI 오류] AI 마음 대화 응답 생성 실패

            발생 시각: ${notification.occurredAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"))}
            환경: ${notification.environment}
            모델: ${notification.model}
            회원 ID: ${notification.memberId}
            대화방 ID: ${notification.roomId}
            메시지 ID: ${notification.messageId}
            실패 유형: ${notification.failureType}
            fallback 사용 여부: ${if (notification.fallbackUsed) "Y" else "N"}

            조치 필요:
            OPENAI_API_KEY, 모델명, 과금 상태, rate limit을 확인하세요.
        """.trimIndent()
    }

    private fun buildInquiryReceivedMessage(notification: InquiryReceivedNotification): String {
        return """
            [myMentalCare 문의 접수] 새 문의가 접수되었습니다

            접수 시각: ${notification.receivedAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"))}
            문의 ID: ${notification.inquiryId}
            회원 ID: ${notification.memberId}
            문의 유형: ${notification.category}

            문의 내용:
            ${notification.content.take(DISCORD_INQUIRY_CONTENT_LIMIT)}
        """.trimIndent()
    }
}
