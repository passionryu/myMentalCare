package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.application.port.ChatMessageRepository
import com.mymentalcare.server.domain.aichat.ChatMessage
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class ChatMessagePersistenceAdapter(
    private val jpaChatMessageRepository: JpaChatMessageRepository,
) : ChatMessageRepository {
    override fun findByRoomId(roomId: Long): List<ChatMessage> {
        return jpaChatMessageRepository.findByRoomIdOrderByMessageOrderAsc(roomId)
            .map { it.toDomain() }
    }

    override fun findBySegmentId(segmentId: Long): List<ChatMessage> {
        return jpaChatMessageRepository.findBySegmentIdOrderByMessageOrderAsc(segmentId)
            .map { it.toDomain() }
    }

    override fun findRecentByRoomId(roomId: Long, limit: Int): List<ChatMessage> {
        return jpaChatMessageRepository.findByRoomIdOrderByMessageOrderDesc(roomId, PageRequest.of(0, limit))
            .asReversed()
            .map { it.toDomain() }
    }

    override fun countByRoomId(roomId: Long): Int {
        return jpaChatMessageRepository.countByRoomId(roomId)
    }

    override fun save(message: ChatMessage): ChatMessage {
        return jpaChatMessageRepository.save(message.toEntity()).toDomain()
    }
}
