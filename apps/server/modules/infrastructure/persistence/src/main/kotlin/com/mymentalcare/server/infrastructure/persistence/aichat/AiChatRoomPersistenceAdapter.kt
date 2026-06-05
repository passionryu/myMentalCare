package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.application.port.AiChatRoomRepository
import com.mymentalcare.server.domain.aichat.AiChatRoom
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AiChatRoomPersistenceAdapter(
    private val jpaAiChatRoomRepository: JpaAiChatRoomRepository,
) : AiChatRoomRepository {
    override fun findTodayRoom(memberId: Long, chatbotCode: String, conversationDate: LocalDate): AiChatRoom? {
        return jpaAiChatRoomRepository.findByMemberIdAndChatbotCodeAndConversationDate(
            memberId = memberId,
            chatbotCode = chatbotCode,
            conversationDate = conversationDate,
        )?.toDomain()
    }

    override fun save(room: AiChatRoom): AiChatRoom {
        return jpaAiChatRoomRepository.save(room.toEntity()).toDomain()
    }
}
