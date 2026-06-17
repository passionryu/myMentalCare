package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.application.aichat.port.AiChatRoomSummaryRepository
import com.mymentalcare.server.domain.aichat.AiChatRoomSummary
import org.springframework.stereotype.Repository

@Repository
class AiChatRoomSummaryPersistenceAdapter(
    private val jpaAiChatRoomSummaryRepository: JpaAiChatRoomSummaryRepository,
) : AiChatRoomSummaryRepository {
    override fun findByRoomId(roomId: Long): AiChatRoomSummary? {
        return jpaAiChatRoomSummaryRepository.findByRoomId(roomId)?.toDomain()
    }

    override fun save(summary: AiChatRoomSummary): AiChatRoomSummary {
        return jpaAiChatRoomSummaryRepository.save(summary.toEntity()).toDomain()
    }
}
