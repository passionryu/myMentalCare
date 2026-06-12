package com.mymentalcare.server.domain.aichat

import java.time.LocalDateTime

data class CrisisDetectionEvent(
    val id: Long,
    val memberId: Long,
    val roomId: Long,
    val sourceType: CrisisDetectionSourceType = CrisisDetectionSourceType.MESSAGE,
    val messageId: Long?,
    val checkInId: Long? = null,
    val detectedKeywords: List<String>,
    val riskLevel: CrisisRiskLevel,
    val handledAction: CrisisHandledAction,
    val createdAt: LocalDateTime? = null,
)
