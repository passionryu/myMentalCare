package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.application.aichat.policy.*
import com.mymentalcare.server.application.aichat.port.*
import com.mymentalcare.server.application.aichat.reader.*
import com.mymentalcare.server.application.aichat.recorder.*
import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*
import com.mymentalcare.server.application.aichat.usecase.*

import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.AiChatRoomStatus
import com.mymentalcare.server.domain.aichat.AiChatRoomSummary
import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AiReplyContextReaderTest {
    @Test
    fun `Redis 최근 메시지 캐시가 있으면 RDB 조회 없이 캐시를 사용한다`() {
        val messageRepository = FakeChatMessageRepository()
        val recentMessageCache = FakeAiChatRecentMessageCache(
            cachedMessages = mutableListOf(
                AiReplyMessage(ChatMessageSenderType.USER, "캐시된 사용자 메시지"),
                AiReplyMessage(ChatMessageSenderType.ASSISTANT, "캐시된 마음이 응답"),
            )
        )
        val reader = aiReplyContextReader(messageRepository, recentMessageCache)

        val recentMessages = reader.readRecentMessagesForReply(todayRoom(), chatMessage(3, "새 사용자 메시지"))

        assertEquals(0, messageRepository.findRecentCallCount)
        assertEquals("캐시된 사용자 메시지", recentMessages.first().content)
        assertEquals("새 사용자 메시지", recentMessages.last().content)
    }

    @Test
    fun `Redis 최근 메시지 캐시가 없으면 RDB 최근 메시지로 캐시를 복구한다`() {
        val messageRepository = FakeChatMessageRepository()
        val recentMessageCache = FakeAiChatRecentMessageCache()
        repeat(7) { index ->
            messageRepository.save(chatMessage(index + 1, "RDB 메시지 ${index + 1}"))
        }
        val reader = aiReplyContextReader(messageRepository, recentMessageCache)

        val recentMessages = reader.readRecentMessagesForReply(todayRoom(), chatMessage(7, "RDB 메시지 7"))

        assertEquals(1, messageRepository.findRecentCallCount)
        assertEquals(6, recentMessages.size)
        assertEquals("RDB 메시지 2", recentMessages.first().content)
        assertEquals("RDB 메시지 7", recentMessages.last().content)
        assertEquals(recentMessages, recentMessageCache.cachedMessages)
    }

    @Test
    fun `새 메시지를 최근 메시지 캐시에 추가할 때 최대 개수만 유지한다`() {
        val recentMessageCache = FakeAiChatRecentMessageCache(
            cachedMessages = MutableList(6) { index ->
                AiReplyMessage(ChatMessageSenderType.USER, "기존 캐시 메시지 ${index + 1}")
            }
        )
        val reader = aiReplyContextReader(recentMessageCache = recentMessageCache)

        reader.appendMessagesToRecentCache(todayRoom(), listOf(chatMessage(7, "새 마음이 응답")))

        assertEquals(6, recentMessageCache.cachedMessages.size)
        assertEquals("기존 캐시 메시지 2", recentMessageCache.cachedMessages.first().content)
        assertEquals("새 마음이 응답", recentMessageCache.cachedMessages.last().content)
    }

    private fun aiReplyContextReader(
        messageRepository: FakeChatMessageRepository = FakeChatMessageRepository(),
        recentMessageCache: FakeAiChatRecentMessageCache = FakeAiChatRecentMessageCache(),
    ): AiReplyContextReader {
        return AiReplyContextReader(
            chatMessageRepository = messageRepository,
            aiChatRoomSummaryRepository = FakeAiChatRoomSummaryRepository(),
            aiChatRecentMessageCache = recentMessageCache,
        )
    }

    private fun todayRoom(): AiChatRoom {
        return AiChatRoom(
            id = 1L,
            memberId = 1L,
            chatbotCode = "DEFAULT_EMPATHY",
            conversationDate = LocalDate.now(),
            status = AiChatRoomStatus.ACTIVE,
        )
    }

    private fun chatMessage(order: Int, content: String): ChatMessage {
        return ChatMessage(
            id = order.toLong(),
            roomId = 1L,
            senderType = ChatMessageSenderType.USER,
            content = content,
            messageOrder = order,
            isCrisisDetected = false,
        )
    }

    private class FakeAiChatRoomSummaryRepository : AiChatRoomSummaryRepository {
        override fun findByRoomId(roomId: Long): AiChatRoomSummary? = null

        override fun save(summary: AiChatRoomSummary): AiChatRoomSummary = summary
    }

    private class FakeChatMessageRepository : ChatMessageRepository {
        private val messages = mutableListOf<ChatMessage>()
        var findRecentCallCount = 0

        override fun findByRoomId(roomId: Long): List<ChatMessage> {
            return messages.filter { it.roomId == roomId }.sortedBy { it.messageOrder }
        }

        override fun findBySegmentId(segmentId: Long): List<ChatMessage> {
            return messages.filter { it.segmentId == segmentId }.sortedBy { it.messageOrder }
        }

        override fun findRecentByRoomId(roomId: Long, limit: Int): List<ChatMessage> {
            findRecentCallCount += 1
            return findByRoomId(roomId).takeLast(limit)
        }

        override fun countByRoomId(roomId: Long): Int {
            return messages.count { it.roomId == roomId }
        }

        override fun findLatestByRoomId(roomId: Long): ChatMessage? {
            return findByRoomId(roomId).maxByOrNull { it.createdAt ?: java.time.LocalDateTime.MIN }
        }

        override fun save(message: ChatMessage): ChatMessage {
            messages.add(message)
            return message
        }
    }

    private class FakeAiChatRecentMessageCache(
        val cachedMessages: MutableList<AiReplyMessage> = mutableListOf(),
    ) : AiChatRecentMessageCache {
        override fun readRecentMessages(roomId: Long): List<AiReplyMessage> {
            return cachedMessages.toList()
        }

        override fun replaceRecentMessages(roomId: Long, messages: List<AiReplyMessage>) {
            cachedMessages.clear()
            cachedMessages.addAll(messages)
        }

        override fun appendRecentMessages(roomId: Long, messages: List<AiReplyMessage>, limit: Int) {
            val mergedMessages = (cachedMessages + messages).takeLast(limit)
            cachedMessages.clear()
            cachedMessages.addAll(mergedMessages)
        }
    }
}
