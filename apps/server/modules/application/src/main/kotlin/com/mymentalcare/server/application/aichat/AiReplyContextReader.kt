package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.application.port.AiChatRecentMessageCache
import com.mymentalcare.server.application.port.AiChatRoomSummaryRepository
import com.mymentalcare.server.application.port.ChatMessageRepository
import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.ChatMessage
import org.springframework.stereotype.Component

private const val AI_REPLY_RECENT_MESSAGE_LIMIT = 6

@Component
internal class AiReplyContextReader(
    private val chatMessageRepository: ChatMessageRepository,
    private val aiChatRoomSummaryRepository: AiChatRoomSummaryRepository,
    private val aiChatRecentMessageCache: AiChatRecentMessageCache,
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
    fun readRecentMessagesForReply(room: AiChatRoom, latestUserMessage: ChatMessage): List<AiReplyMessage> {
        val cachedMessages = aiChatRecentMessageCache.readRecentMessages(room.id)
        if (cachedMessages.isNotEmpty()) {
            val recentMessages = (cachedMessages + latestUserMessage.toAiReplyMessage())
                .takeLast(AI_REPLY_RECENT_MESSAGE_LIMIT)
            aiChatRecentMessageCache.replaceRecentMessages(room.id, recentMessages)
            return recentMessages
        }

        val recentMessages = chatMessageRepository.findRecentByRoomId(room.id, AI_REPLY_RECENT_MESSAGE_LIMIT)
            .map { it.toAiReplyMessage() }

        aiChatRecentMessageCache.replaceRecentMessages(room.id, recentMessages)
        return recentMessages
    }

    // 저장된 메시지를 Redis 최근 메시지 캐시에 추가하여 다음 AI 응답 맥락에 바로 반영한다.
    fun appendMessagesToRecentCache(room: AiChatRoom, messages: List<ChatMessage>) {
        aiChatRecentMessageCache.appendRecentMessages(
            room.id,
            messages.map { it.toAiReplyMessage() },
            AI_REPLY_RECENT_MESSAGE_LIMIT,
        )
    }

    private fun ChatMessage.toAiReplyMessage(): AiReplyMessage {
        return AiReplyMessage(
            senderType = senderType,
            content = content,
        )
    }
}
