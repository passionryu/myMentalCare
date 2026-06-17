package com.mymentalcare.server.application.aichat.request

import com.mymentalcare.server.application.aichat.response.*

data class CreateAiChatReportRequest(
    val forceCreate: Boolean,
    val clientRequestId: String?,
)
