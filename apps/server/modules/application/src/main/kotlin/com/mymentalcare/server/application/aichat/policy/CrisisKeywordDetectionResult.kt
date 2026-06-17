package com.mymentalcare.server.application.aichat.policy

import com.mymentalcare.server.application.aichat.port.*
import com.mymentalcare.server.application.aichat.request.*
import com.mymentalcare.server.application.aichat.response.*

import com.mymentalcare.server.domain.aichat.CrisisRiskLevel

data class CrisisKeywordDetectionResult(
    val detected: Boolean,
    val keywords: List<String>,
    val riskLevel: CrisisRiskLevel,
)
