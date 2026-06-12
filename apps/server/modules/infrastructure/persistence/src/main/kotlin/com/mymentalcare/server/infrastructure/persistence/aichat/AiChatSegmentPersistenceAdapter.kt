package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.application.port.AiChatSegmentRepository
import com.mymentalcare.server.domain.aichat.AiChatSegment
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class AiChatSegmentPersistenceAdapter(
    private val jpaAiChatSegmentRepository: JpaAiChatSegmentRepository,
) : AiChatSegmentRepository {
    override fun findById(segmentId: Long): AiChatSegment? {
        return jpaAiChatSegmentRepository.findByIdOrNull(segmentId)?.toDomain()
    }

    override fun findByRoomId(roomId: Long): List<AiChatSegment> {
        return jpaAiChatSegmentRepository.findByRoomIdOrderBySegmentOrderAsc(roomId)
            .map { it.toDomain() }
    }

    override fun findLatestByRoomId(roomId: Long): AiChatSegment? {
        return jpaAiChatSegmentRepository.findFirstByRoomIdOrderBySegmentOrderDesc(roomId)?.toDomain()
    }

    override fun findByRoomIdAndClientRequestId(roomId: Long, clientRequestId: String): AiChatSegment? {
        return jpaAiChatSegmentRepository.findByRoomIdAndClientRequestId(roomId, clientRequestId)?.toDomain()
    }

    override fun countByRoomId(roomId: Long): Int {
        return jpaAiChatSegmentRepository.countByRoomId(roomId)
    }

    override fun save(segment: AiChatSegment): AiChatSegment {
        return jpaAiChatSegmentRepository.save(segment.toEntity()).toDomain()
    }
}
