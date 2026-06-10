package com.mymentalcare.server.application.port

import com.mymentalcare.server.domain.aichat.CrisisDetectionEvent

interface CrisisDetectionEventRepository {
    fun save(event: CrisisDetectionEvent): CrisisDetectionEvent
}
