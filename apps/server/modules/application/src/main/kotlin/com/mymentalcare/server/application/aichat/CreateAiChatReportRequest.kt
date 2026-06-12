package com.mymentalcare.server.application.aichat

data class CreateAiChatReportRequest(
    val forceCreate: Boolean,
    val clientRequestId: String?,
)
