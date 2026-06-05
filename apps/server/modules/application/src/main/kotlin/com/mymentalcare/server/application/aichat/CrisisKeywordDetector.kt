package com.mymentalcare.server.application.aichat

import com.mymentalcare.server.domain.aichat.CrisisRiskLevel
import org.springframework.stereotype.Component

const val SAFETY_GUIDE_MESSAGE = "지금은 혼자 견디지 않아도 됩니다. 즉시 주변 사람이나 전문 기관에 도움을 요청해주세요. 긴급한 위험이 있다면 112 또는 119에 연락하고, 자살예방상담전화 109에도 도움을 요청할 수 있습니다."

private val crisisKeywords = listOf(
    "죽고 싶다",
    "죽고싶다",
    "자살",
    "자해",
    "사라지고 싶다",
    "끝내고 싶다",
    "살기 싫다",
    "해치고 싶다",
)

@Component
class CrisisKeywordDetector {
    // 사용자 메시지에서 위기 표현 키워드를 감지한다.
    fun detectCrisisKeywords(content: String): CrisisKeywordDetectionResult {
        val normalized = content.lowercase().replace(" ", "")
        val detectedKeywords = crisisKeywords.filter { keyword ->
            normalized.contains(keyword.replace(" ", "").lowercase())
        }

        return CrisisKeywordDetectionResult(
            detected = detectedKeywords.isNotEmpty(),
            keywords = detectedKeywords,
            riskLevel = if (detectedKeywords.isEmpty()) CrisisRiskLevel.LOW else CrisisRiskLevel.HIGH,
        )
    }
}
