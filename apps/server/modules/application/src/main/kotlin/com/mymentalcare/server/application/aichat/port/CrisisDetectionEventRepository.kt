package com.mymentalcare.server.application.aichat.port

import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.CrisisDetectionEvent

interface CrisisDetectionEventRepository {
    fun save(event: CrisisDetectionEvent): CrisisDetectionEvent
}
