package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.application.port.AiChatRoomSummaryRepository
import com.mymentalcare.server.application.port.ChatMessageRepository
import com.mymentalcare.server.domain.aichat.AiChatRoom
import org.springframework.stereotype.Component

private const val AI_REPLY_RECENT_MESSAGE_LIMIT = 6

@Component
internal class AiReplyContextReader(
    private val chatMessageRepository: ChatMessageRepository,
    private val aiChatRoomSummaryRepository: AiChatRoomSummaryRepository,
) {
    // AI 응답 생성에 사용할 오늘 대화 요약 메모리를 읽는다.
    fun readTodaySummaryContext(room: AiChatRoom): AiChatSummaryContext? {
        val summary = aiChatRoomSummaryRepository.findByRoomId(room.id) ?: return null
        return AiChatSummaryContext(
            summary = summary.summary,
            emotionalState = summary.emotionalState,
            activeTopics = summary.activeTopics,
            unresolvedQuestions = summary.unresolvedQuestions,
            userPreferences = summary.userPreferences,
        )
    }

    // AI 응답 생성에 사용할 최근 대화 메시지만 추려낸다.
    fun readRecentMessagesForReply(room: AiChatRoom): List<AiReplyMessage> {
        return chatMessageRepository.findByRoomId(room.id)
            .takeLast(AI_REPLY_RECENT_MESSAGE_LIMIT)
            .map {
                AiReplyMessage(
                    senderType = it.senderType,
                    content = it.content,
                )
            }
    }
}
