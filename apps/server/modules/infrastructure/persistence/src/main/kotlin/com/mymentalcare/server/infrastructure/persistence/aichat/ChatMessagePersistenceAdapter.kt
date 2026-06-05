package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.application.port.ChatMessageRepository
import com.mymentalcare.server.domain.aichat.ChatMessage
import org.springframework.stereotype.Repository

@Repository
class ChatMessagePersistenceAdapter(
    private val jpaChatMessageRepository: JpaChatMessageRepository,
) : ChatMessageRepository {
    override fun findByRoomId(roomId: Long): List<ChatMessage> {
        return jpaChatMessageRepository.findByRoomIdOrderByMessageOrderAsc(roomId)
            .map { it.toDomain() }
    }

    override fun countByRoomId(roomId: Long): Int {
        return jpaChatMessageRepository.countByRoomId(roomId)
    }

    override fun save(message: ChatMessage): ChatMessage {
        return jpaChatMessageRepository.save(message.toEntity()).toDomain()
    }
}
