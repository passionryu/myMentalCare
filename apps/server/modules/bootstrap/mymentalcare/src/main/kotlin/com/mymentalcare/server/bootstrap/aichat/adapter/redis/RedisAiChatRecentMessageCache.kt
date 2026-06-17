package com.mymentalcare.server.bootstrap.aichat.adapter.redis

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.mymentalcare.server.application.aichat.request.AiReplyMessage
import com.mymentalcare.server.application.aichat.port.AiChatRecentMessageCache
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

private val KOREA_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
private val CACHED_MESSAGE_LIST_TYPE = object : TypeReference<List<CachedAiReplyMessage>>() {}

@Component
class RedisAiChatRecentMessageCache(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : AiChatRecentMessageCache {
    override fun readRecentMessages(roomId: Long): List<AiReplyMessage> {
        val cachedValue = redisTemplate.opsForValue().get(cacheKey(roomId)) ?: return emptyList()
        return objectMapper.readValue(cachedValue, CACHED_MESSAGE_LIST_TYPE)
            .map { it.toAiReplyMessage() }
    }

    override fun replaceRecentMessages(roomId: Long, messages: List<AiReplyMessage>) {
        writeMessages(roomId, messages)
    }

    override fun appendRecentMessages(roomId: Long, messages: List<AiReplyMessage>, limit: Int) {
        val mergedMessages = (readRecentMessages(roomId) + messages).takeLast(limit)
        writeMessages(roomId, mergedMessages)
    }

    // 한국 시간 기준 오늘 대화가 끝날 때 최근 메시지 캐시도 자연스럽게 만료되게 한다.
    private fun todayConversationTtl(): Duration {
        val now = ZonedDateTime.now(KOREA_ZONE_ID)
        val tomorrowStart = LocalDate.now(KOREA_ZONE_ID).plusDays(1).atStartOfDay(KOREA_ZONE_ID)
        return Duration.between(now, tomorrowStart).coerceAtLeast(Duration.ofMinutes(1))
    }

    private fun writeMessages(roomId: Long, messages: List<AiReplyMessage>) {
        val cachedMessages = messages.map { CachedAiReplyMessage(it.senderType, it.content) }
        redisTemplate.opsForValue().set(
            cacheKey(roomId),
            objectMapper.writeValueAsString(cachedMessages),
            todayConversationTtl(),
        )
    }

    private fun cacheKey(roomId: Long): String {
        return "ai-chat:room:$roomId:recent-messages"
    }
}

private data class CachedAiReplyMessage(
    val senderType: ChatMessageSenderType,
    val content: String,
) {
    fun toAiReplyMessage(): AiReplyMessage {
        return AiReplyMessage(
            senderType = senderType,
            content = content,
        )
    }
}
