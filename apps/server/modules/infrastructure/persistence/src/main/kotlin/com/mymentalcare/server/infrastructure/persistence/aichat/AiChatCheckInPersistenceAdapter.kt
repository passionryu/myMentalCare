package com.mymentalcare.server.infrastructure.persistence.aichat

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.mymentalcare.server.application.port.AiChatCheckInRepository
import com.mymentalcare.server.domain.aichat.AiChatCheckIn
import com.mymentalcare.server.domain.aichat.AiChatCheckInAnswer
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class AiChatCheckInPersistenceAdapter(
    private val jpaAiChatCheckInRepository: JpaAiChatCheckInRepository,
    private val objectMapper: ObjectMapper,
) : AiChatCheckInRepository {
    override fun findBySegmentId(segmentId: Long): AiChatCheckIn? {
        return jpaAiChatCheckInRepository.findBySegmentId(segmentId)?.toDomain()
    }

    override fun findBySegmentIds(segmentIds: List<Long>): List<AiChatCheckIn> {
        if (segmentIds.isEmpty()) {
            return emptyList()
        }

        return jpaAiChatCheckInRepository.findBySegmentIdIn(segmentIds).map { it.toDomain() }
    }

    override fun save(checkIn: AiChatCheckIn): AiChatCheckIn {
        return jpaAiChatCheckInRepository.save(checkIn.toEntity()).toDomain()
    }

    private fun AiChatCheckInEntity.toDomain(): AiChatCheckIn {
        return AiChatCheckIn(
            id = id,
            segmentId = segmentId,
            templateType = templateType,
            answers = objectMapper.readValue(answersJson, object : TypeReference<List<AiChatCheckInAnswer>>() {}),
            summaryText = summaryText,
            isCrisisDetected = isCrisisDetected,
            detectedKeywords = detectedKeywords?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
            createdAt = createdAt,
        )
    }

    private fun AiChatCheckIn.toEntity(): AiChatCheckInEntity {
        return AiChatCheckInEntity(
            id = id,
            segmentId = segmentId,
            templateType = templateType,
            answersJson = objectMapper.writeValueAsString(answers),
            summaryText = summaryText,
            isCrisisDetected = isCrisisDetected,
            detectedKeywords = detectedKeywords.joinToString(",").takeIf { it.isNotBlank() },
            createdAt = createdAt ?: LocalDateTime.now(),
        )
    }
}
