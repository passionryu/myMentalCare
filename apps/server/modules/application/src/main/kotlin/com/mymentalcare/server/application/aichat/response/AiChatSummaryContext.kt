package com.mymentalcare.server.application.aichat.response

data class AiChatSummaryContext(
    val summary: String,
    val emotionalState: String?,
    val activeTopics: String?,
    val unresolvedQuestions: String?,
    val userPreferences: String?,
)
