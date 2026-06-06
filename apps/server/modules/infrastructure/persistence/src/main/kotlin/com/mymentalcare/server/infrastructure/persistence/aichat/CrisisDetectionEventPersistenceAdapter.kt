package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.application.port.CrisisDetectionEventRepository
import com.mymentalcare.server.domain.aichat.CrisisDetectionEvent
import org.springframework.stereotype.Repository

@Repository
class CrisisDetectionEventPersistenceAdapter(
    private val jpaCrisisDetectionEventRepository: JpaCrisisDetectionEventRepository,
) : CrisisDetectionEventRepository {
    override fun save(event: CrisisDetectionEvent): CrisisDetectionEvent {
        return jpaCrisisDetectionEventRepository.save(event.toEntity()).toDomain()
    }
}
