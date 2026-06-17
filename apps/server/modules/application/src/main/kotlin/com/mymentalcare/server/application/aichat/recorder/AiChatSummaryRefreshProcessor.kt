package com.mymentalcare.server.application.aichat.recorder

import com.mymentalcare.server.application.aichat.policy.*
import com.mymentalcare.server.application.aichat.port.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.application.common.extension.logWarn
import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.AiChatRoomSummary
import org.springframework.stereotype.Component

@Component
internal class AiChatSummaryRefreshProcessor(
    private val aiChatRoomSummaryRepository: AiChatRoomSummaryRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val aiChatSummaryRefreshDecider: AiChatSummaryRefreshDecider,
    private val aiChatSummaryGenerator: AiChatSummaryGenerator,
) {
    // 오늘 대화에서 아직 요약되지 않은 메시지가 충분하면 요약 메모리를 갱신한다.
    fun refreshTodaySummaryIfNeeded(room: AiChatRoom): AiChatRoomSummary? {
        val existingSummary = aiChatRoomSummaryRepository.findByRoomId(room.id)
        val lastSummarizedMessageId = existingSummary?.lastSummarizedMessageId
        val unsummarizedMessages = chatMessageRepository.findByRoomId(room.id)
            .filter { message ->
                lastSummarizedMessageId == null || message.id > lastSummarizedMessageId
            }

        if (!aiChatSummaryRefreshDecider.shouldRefreshTodaySummary(unsummarizedMessages)) {
            return existingSummary
        }

        return try {
            val generatedSummary = aiChatSummaryGenerator.generateTodaySummary(existingSummary, unsummarizedMessages)
            aiChatRoomSummaryRepository.save(
                AiChatRoomSummary(
                    id = existingSummary?.id ?: 0,
                    roomId = room.id,
                    memberId = room.memberId,
                    lastSummarizedMessageId = unsummarizedMessages.maxOfOrNull { it.id },
                    summary = generatedSummary.summary,
                    emotionalState = generatedSummary.emotionalState,
                    activeTopics = generatedSummary.activeTopics,
                    unresolvedQuestions = generatedSummary.unresolvedQuestions,
                    userPreferences = generatedSummary.userPreferences,
                    createdAt = existingSummary?.createdAt,
                    updatedAt = existingSummary?.updatedAt,
                )
            )
        } catch (e: RuntimeException) {
            logWarn {
                "[AI 마음 대화] 오늘 대화 요약 메모리 갱신 실패. " +
                    "who=memberId:${room.memberId}, " +
                    "what=AiChatSummaryRefreshProcessor.refreshTodaySummaryIfNeeded, " +
                    "requestData=roomId:${room.id},unsummarizedMessageCount:${unsummarizedMessages.size}, " +
                    "reason=${e.message ?: "unknown"}"
            }
            existingSummary
        }
    }
}
