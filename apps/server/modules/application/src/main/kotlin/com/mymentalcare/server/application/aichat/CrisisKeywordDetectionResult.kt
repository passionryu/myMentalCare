package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.domain.aichat.CrisisRiskLevel

data class CrisisKeywordDetectionResult(
    val detected: Boolean,
    val keywords: List<String>,
    val riskLevel: CrisisRiskLevel,
)
